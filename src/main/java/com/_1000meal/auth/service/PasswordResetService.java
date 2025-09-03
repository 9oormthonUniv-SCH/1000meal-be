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
import com._1000meal.global.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;

    @Value("${app.password-reset.token-ttl-minutes:10}")
    private long tokenTtlMinutes;

    @Value("${app.password-reset.request-cooldown-seconds:60}")
    private long cooldownSeconds;

    /** 비로그인: 재설정 코드(6자리) 발급 & 메일 전송 */
    @Transactional
    public void requestReset(PasswordResetRequest req) {
        Account account = accountRepository.findByEmail(req.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 레이트리밋
        LocalDateTime after = LocalDateTime.now().minusSeconds(cooldownSeconds);
        if (tokenRepository.countByAccountAndCreatedAtAfter(account, after) > 0) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // ✅ 기존 미사용 토큰이 있다면 무효화 후 DB 반영
        tokenRepository.findTopByAccountAndUsedAtIsNullOrderByIdDesc(account)
                .ifPresent(oldToken -> {
                    oldToken.markUsed();
                    tokenRepository.saveAndFlush(oldToken); // 즉시 UPDATE 반영
                });

        // 새 토큰 발급
        PasswordResetToken token = PasswordResetToken.issue(account, tokenTtlMinutes);
        tokenRepository.save(token);

        // 메일 본문: 인증 코드 표시
        String subject = "[1000Meal] 비밀번호 재설정 인증 코드";
        String body = """
            <p>안녕하세요.</p>
            <p>아래 인증 코드를 입력하여 비밀번호를 재설정해 주세요.</p>
            <p style="font-size:18px;font-weight:bold;">인증 코드: %s</p>
            <p>유효시간: %d분</p>
            <p>본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.</p>
            """.formatted(token.getRawToken(), tokenTtlMinutes);

        mailSender.send(account.getEmail(), subject, body);
    }

    /** 비로그인: 이메일 + 6자리 코드 + 새 비밀번호로 재설정 */
    @Transactional
    public void confirmReset(PasswordResetConfirmRequest req) {
        Account account = accountRepository.findByEmail(req.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 토큰 조회: 평문 코드를 SHA-256 해시로 변환 후 매칭
        String tokenHash = sha256Hex(req.token());
        PasswordResetToken token = tokenRepository
                .findTopByAccountAndTokenHashAndUsedAtIsNullAndExpiresAtAfterOrderByIdDesc(
                        account, tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        // 비밀번호 검증
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);
        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 변경 & 토큰 사용 처리
        account.changePassword(passwordEncoder.encode(req.newPassword()));
        token.markUsed();
    }

    /** SHA-256 해시 유틸 */
    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}