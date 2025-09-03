package com._1000meal.email.common;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
@Primary
@RequiredArgsConstructor
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true → HTML 형식
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패", e);
        }
    }
}