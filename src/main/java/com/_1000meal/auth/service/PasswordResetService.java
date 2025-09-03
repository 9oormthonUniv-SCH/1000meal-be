package com._1000meal.auth.service;

import com._1000meal.auth.dto.PasswordResetConfirmRequest;
import com._1000meal.auth.dto.PasswordResetRequest;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.PasswordResetToken;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.PasswordResetTokenRepository;
import com._1000meal.email.common.MailSender;
import com._1000meal.global.constant.Role;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.util.PasswordValidator;   // ← 기존 유틸 재사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@lombok.RequiredArgsConstructor
public class PasswordResetService {

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;

    @Value("${app.password-reset.token-ttl-minutes:10}")
    private long tokenTtlMinutes;

    @Value("${app.password-reset.request-cooldown-seconds:60}")
    private long cooldownSeconds;

    @Value("${app.password-reset.reset-url-base}")
    private String resetUrlBase;

    /** 비로그인: 재설정 링크 요청 */
    @Transactional
    public void requestReset(PasswordResetRequest req) {
        Account account = accountRepository.findByEmail(req.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 관리자 제외
        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 레이트리밋: 최근 쿨타임 내 요청 거절
        LocalDateTime after = LocalDateTime.now().minusSeconds(cooldownSeconds);
        long recent = tokenRepository.countByAccountAndCreatedAtAfter(account, after);
        if (recent > 0) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 토큰 발급 & 저장
        PasswordResetToken token = PasswordResetToken.issue(account, tokenTtlMinutes);
        tokenRepository.save(token);

        // 링크 생성
        String link = resetUrlBase + "?token=" + URLEncoder.encode(token.getToken(), StandardCharsets.UTF_8);

        // 메일 발송
        String subject = "[1000Meal] 비밀번호 재설정 안내";
        String body = """
                <p>안녕하세요.</p>
                <p>아래 버튼을 눌러 비밀번호 재설정을 진행하세요. 유효시간은 %d분입니다.</p>
                <p><a href="%s" style="display:inline-block;padding:10px 16px;background:#2d6cdf;color:#fff;text-decoration:none;border-radius:6px;">비밀번호 재설정</a></p>
                <p>만약 본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.</p>
                """.formatted(tokenTtlMinutes, link);

        mailSender.send(account.getEmail(), subject, body);
    }

    /** 비로그인: 재설정 실행 */
    @Transactional
    public void confirmReset(PasswordResetConfirmRequest req) {
        PasswordResetToken token = tokenRepository.findByToken(req.token())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!token.isUsable()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Account account = token.getAccount();

        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 기존 유틸로 강도/금지 규칙 검증 (username = userId, phoneNumber는 없으면 null)
        PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);

        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 비밀번호 변경
        account.changePassword(passwordEncoder.encode(req.newPassword()));

        // 토큰 1회성 처리
        token.markUsed();
    }
}