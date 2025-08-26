package com._1000meal.email.service;

import com._1000meal.email.domain.EmailVerificationToken;
import com._1000meal.email.repository.EmailVerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository tokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 6자리 숫자 코드 생성 */
    public String generateCode() {
        return String.valueOf(new Random().nextInt(900_000) + 100_000);
    }

    /** 인증 코드 발급(2분) + 기존 미검증 코드 즉시 무효화 + 메일 발송 이벤트 */
    @Transactional
    public void issueAndStoreCode(String email) {
        final String normalized = email.trim().toLowerCase();

        // 학교 도메인만 허용
        if (!normalized.endsWith("@sch.ac.kr")) {
            throw new IllegalArgumentException("순천향대학교 이메일만 인증할 수 있습니다.");
        }

        // 1) 기존 미검증 코드 즉시 무효화(삭제)
        tokenRepository.deleteByEmailAndVerifiedFalse(normalized);

        // 2) 새 코드 생성 및 저장 (TTL=2분)
        String code = generateCode();
        tokenRepository.save(EmailVerificationToken.create(normalized, code, /*minutes*/ 2));

        // 3) 커밋 후 발송
        eventPublisher.publishEvent(new VerificationMailRequested(normalized, code));
    }

    /** 코드 검증: 최신 미검증 토큰만 허용 */
    @Transactional
    public void verifyCode(String email, String inputCode) {
        final String normalized = email.trim().toLowerCase();

        EmailVerificationToken token = tokenRepository
                .findTop1ByEmailAndVerifiedFalseOrderByIdDesc(normalized)
                .orElseThrow(() -> new IllegalStateException("유효한 인증 요청이 없습니다."));

        if (token.isExpired()) {
            throw new IllegalStateException("인증 코드가 만료되었습니다.");
        }
        if (!token.getCode().equals(inputCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        // 성공: verified=true
        token.markVerified();

        // (선택) 같은 이메일의 다른 미검증 토큰도 함께 제거하고 싶다면 아래 주석 해제
        // tokenRepository.deleteByEmailAndVerifiedFalse(normalized);
    }

    /** 단순 조회: verified=true 존재 여부 */
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return tokenRepository.existsByEmailAndVerifiedTrue(email.trim().toLowerCase());
    }

    /** 가입 직전 강제확인(verified=true 최신 토큰이 유효해야 함) */
    @Transactional(readOnly = true)
    public void requireVerified(String email) {
        final String normalized = email.trim().toLowerCase();

        EmailVerificationToken token = tokenRepository
                .findTop1ByEmailAndVerifiedTrueOrderByIdDesc(normalized)
                .orElseThrow(() -> new IllegalStateException("이메일 인증이 완료되지 않았습니다."));

        if (token.isExpired()) {
            throw new IllegalStateException("이메일 인증이 만료되었습니다. 다시 인증해 주세요.");
        }
    }

    /** 가입 완료 후 깔끔 정리: 해당 이메일의 모든 토큰 제거 */
    @Transactional
    public void consumeAllFor(String email) {
        tokenRepository.deleteByEmail(email.trim().toLowerCase());
    }

    /** 메일 발송 이벤트 페이로드 */
    public static record VerificationMailRequested(String email, String code) {}
}