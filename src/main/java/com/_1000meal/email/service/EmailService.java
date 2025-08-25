package com._1000meal.email.service;

import com._1000meal.email.domain.EmailVerificationToken;
import com._1000meal.email.dto.EmailSendRequest;
import com._1000meal.email.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60); // 60초 내 재요청 방지

    private final EmailVerificationTokenRepository tokenRepository;
    private final ApplicationEventPublisher publisher;

    /** 6자리 숫자 코드 생성 (보안 RNG) */
    public String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /** 이메일 정규화: trim + toLowerCase */
    private String normalize(String raw) {
        return raw == null ? null : raw.trim().toLowerCase();
    }

    /** 도메인 정책 검사 */
    private void ensureSch(String email) {
        if (!email.endsWith("@sch.ac.kr")) {
            throw new IllegalArgumentException("순천향대학교 이메일만 인증할 수 있습니다.");
        }
    }

    /**
     * 인증 코드 발급: (1) 도메인/상태 검증 → (2) 레이트리밋 확인 → (3) 기존 미검증 토큰 정리 → (4) 새 토큰 저장 → (5) 커밋 후 메일 발송 이벤트
     */
    @Transactional
    public void issueAndStoreCode(String rawEmail) {
        final String email = normalize(rawEmail);
        ensureSch(email);

        // 이미 인증 완료 이메일 차단
        if (tokenRepository.existsByEmailAndVerifiedTrue(email)) {
            throw new IllegalStateException("이미 인증이 완료된 이메일입니다.");
        }

        // 최근 발급 쿨다운(동일 이메일, 최신 미검증 토큰 기준)
        tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc(email).ifPresent(latest -> {
            if (java.time.Duration.between(latest.getCreatedAt(), java.time.LocalDateTime.now())
                    .compareTo(RESEND_COOLDOWN) < 0) {
                throw new IllegalStateException("인증 코드를 너무 자주 요청했습니다. 잠시 후 다시 시도해주세요.");
            }
            if (!latest.isExpired()) {
                latest.expire();
            }
        });

        // 새 코드 발급 및 저장 (유효시간 5분)
        String code = generateCode();
        EmailVerificationToken token = EmailVerificationToken.create(email, code, 5);
        tokenRepository.save(token);

        // 커밋 이후에만 메일 발송 (유령 메일 방지)
        publisher.publishEvent(new VerificationMailRequested(email, code));
    }

    /** 코드 검증 */
    @Transactional
    public void verifyCode(String rawEmail, String inputCode) {
        final String email = normalize(rawEmail);
        var token = tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalStateException("유효한 인증 요청이 없습니다."));

        if (token.isExpired()) {
            throw new IllegalStateException("인증 코드가 만료되었습니다.");
        }
        if (!token.getCode().equals(inputCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        token.markVerified(); // JPA dirty checking
    }

    /** 회원가입 시 이메일 인증 여부 체크 */
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String rawEmail) {
        final String email = normalize(rawEmail);
        return tokenRepository.existsByEmailAndVerifiedTrue(email);
    }

    /** 커밋 후 발송 이벤트 payload */
    public record VerificationMailRequested(String email, String code) {}
}