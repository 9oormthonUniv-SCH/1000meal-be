package com._1000meal.auth.service;


import com._1000meal.auth.dto.ChangePasswordRequest;
import com._1000meal.auth.dto.PasswordResetConfirmRequest;
import com._1000meal.auth.dto.PasswordResetRequest;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AdminPasswordResetToken;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.AdminPasswordResetTokenRepository;
import com._1000meal.global.constant.Role;
import io.jsonwebtoken.lang.Assert;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPasswordService {

    private final AccountRepository accountRepo;
    private final AdminPasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    /* 로그인한 관리자: 비밀번호 변경 */
    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest req) {
        AuthPrincipal p = (AuthPrincipal) authentication.getPrincipal(); // <- 캐스팅

        // ★ PK로 조회 (권장)
        Account account = accountRepo.findById(p.id())
                .orElseThrow(() -> new IllegalStateException("계정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.oldPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        account.changePassword(passwordEncoder.encode(req.newPassword())); // 엔티티에 변경 메서드 추가해둔 것 사용
    }

    /* 로그인 없이: 리셋 메일(토큰) 요청 */
    @Transactional
    public String requestReset(PasswordResetRequest req) {
        String key = req.userIdOrEmail().trim().toLowerCase();

        Account account = accountRepo.findByUserIdOrEmail(key, key)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        if (account.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정만 비밀번호 재설정이 가능합니다.");
        }

        // 토큰 발급 (유효시간 10분 예시)
        AdminPasswordResetToken token = AdminPasswordResetToken.create(account, 10);
        tokenRepo.save(token);

        // 실제로는 이메일 발송 필요(메일 본문에 token 포함한 링크 전달)
        // ex) https://admin.yoursite.com/reset?token=xxxx
        return token.getToken();
    }

    /* 토큰으로 비밀번호 재설정 */
    @Transactional
    public void confirmReset(PasswordResetConfirmRequest req) {
        AdminPasswordResetToken token = tokenRepo.findByTokenAndUsedFalse(req.token())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (token.isExpired()) {
            throw new IllegalStateException("토큰이 만료되었습니다.");
        }

        Account account = token.getAccount();
        if (account.getRole() != Role.ADMIN) {
            throw new IllegalStateException("관리자 계정이 아닙니다.");
        }

        account.changePassword(passwordEncoder.encode(req.newPassword())); // 변경 메서드 필요 시 추가
        token.markUsed();

        // (선택) 같은 계정의 예전 토큰들 정리
        tokenRepo.deleteByAccountId(account.getId());
    }
}