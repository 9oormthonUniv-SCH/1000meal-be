package com._1000meal.email.service;

import com._1000meal.email.domain.EmailVerificationToken;
import com._1000meal.email.dto.EmailSendRequest;
import com._1000meal.email.repository.EmailVerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository tokenRepository;

    public String generateCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    public void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            String fromName = "today_SoonBab";
            String fromEmail = "tejava7177@gmail.com";

            helper.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
            helper.setTo(toEmail);
            helper.setSubject("[오늘 순밥] 이메일 인증 코드입니다.");
            helper.setText("인증 코드는 다음과 같습니다: " + code, false);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    @Transactional
    public void issueAndStoreCode(String email) {
        if (!email.endsWith("@sch.ac.kr")) {
            throw new IllegalArgumentException("순천향대학교 이메일만 인증할 수 있습니다.");
        }
        if (tokenRepository.existsByEmailAndVerifiedTrue(email)) {
            throw new IllegalStateException("이미 인증이 완료된 이메일입니다.");
        }

        String code = generateCode();
        tokenRepository.save(EmailVerificationToken.create(email, code, 5)); // 1) 저장
        sendVerificationEmail(email, code);                                  // 2) 발송(1회)
    }

    @Transactional
    // 2) 코드 검증
    public void verifyCode(String email, String inputCode) {
        var token = tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalStateException("유효한 인증 요청이 없습니다."));

        if (token.isExpired()) {
            throw new IllegalStateException("인증 코드가 만료되었습니다.");
        }
        if (!token.getCode().equals(inputCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        token.markVerified();  // JPA dirty checking
    }

    // 회원가입 시 사용할 “이메일 인증 여부” 체크
    public boolean isEmailVerified(String rawEmail) {
        final String email = rawEmail.trim().toLowerCase();
        return tokenRepository.existsByEmailAndVerifiedTrue(email);
    }
}