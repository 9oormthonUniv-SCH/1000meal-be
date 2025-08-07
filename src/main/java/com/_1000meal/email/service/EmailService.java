package com._1000meal.email.service;

import com._1000meal.email.dto.EmailSendRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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
}