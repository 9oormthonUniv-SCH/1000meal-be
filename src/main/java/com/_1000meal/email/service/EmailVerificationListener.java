package com._1000meal.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class EmailVerificationListener {

    private final JavaMailSender mailSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequested(EmailService.VerificationMailRequested e) {
        sendVerificationEmail(e.email(), e.code());
    }

    private void sendVerificationEmail(String toEmail, String code) {
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
        } catch (Exception ex) {
            // TODO: 로깅/재시도 도입 (Outbox 패턴)
            throw new RuntimeException("이메일 전송 실패", ex);
        }
    }
}