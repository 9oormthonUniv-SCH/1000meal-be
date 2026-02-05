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
        final String email = req.email() == null ? "" : req.email().trim().toLowerCase();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "해당 이메일(" + email + ")로 가입된 계정을 찾을 수 없습니다.")
                );

        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN,
                    "관리자 계정은 비밀번호 재설정을 이메일 코드로 진행할 수 없습니다. 운영팀에 문의하세요."
            );
        }

        // 레이트리밋
        LocalDateTime after = LocalDateTime.now().minusSeconds(cooldownSeconds);
        long recent = tokenRepository.countByAccountAndCreatedAtAfter(account, after);
        if (recent > 0) {
            throw new CustomException(
                    ErrorCode.TOO_MANY_REQUESTS,
                    "요청이 너무 잦습니다. " + cooldownSeconds + "초 후 다시 시도해 주세요."
            );
        }

        // 기존 미사용 토큰 무효화(1회성 보장)
        tokenRepository.findTopByAccountAndUsedAtIsNullOrderByIdDesc(account)
                .ifPresent(oldToken -> {
                    oldToken.markUsed();
                    tokenRepository.saveAndFlush(oldToken);
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
        final String email = req.email() == null ? "" : req.email().trim().toLowerCase();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "해당 이메일(" + email + ")로 가입된 계정을 찾을 수 없습니다.")
                );

        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN,
                    "관리자 계정은 비밀번호 재설정을 이메일 코드로 진행할 수 없습니다. 운영팀에 문의하세요."
            );
        }

        // 토큰 조회: 평문 6자리 코드를 SHA-256 해시로 변환 후 조회
        String tokenHash = sha256Hex(req.token());
        PasswordResetToken token = tokenRepository
                .findTopByAccountAndTokenHashAndUsedAtIsNullAndExpiresAtAfterOrderByIdDesc(
                        account, tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.INVALID_TOKEN,
                        "인증 코드가 유효하지 않거나 만료되었습니다. 새 코드를 요청해 주세요.")
                );

        // 비밀번호 검증
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "새 비밀번호와 확인용 비밀번호가 일치하지 않습니다."
            );
        }

        try {
            PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);
        } catch (CustomException e) {
            // PasswordValidator 내부의 코드/메시지를 그대로 전달
            throw e;
        } catch (Exception e) {
            // 예기치 못한 경우 방어적 메시지
            throw new CustomException(
                    ErrorCode.VALIDATION_ERROR,
                    "비밀번호 형식이 올바르지 않습니다."
            );
        }

        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "새 비밀번호가 이전 비밀번호와 동일합니다."
            );
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