package com._1000meal.email.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationListenerTest {

    @Mock JavaMailSender mailSender;

    @InjectMocks EmailVerificationListener listener;

    @Test
    @DisplayName("이메일 발송 이벤트 수신 시 mailSender.send()가 호출된다")
    void onRequested_sendsEmail() {
        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        when(mailSender.createMimeMessage()).thenReturn(message);

        listener.onRequested(new EmailService.VerificationMailRequested("user@sch.ac.kr", "123456"));

        verify(mailSender).send(message);
    }

    @Test
    @DisplayName("메일 전송 중 예외 발생 시 RuntimeException으로 래핑된다")
    void onRequested_wrapsException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> listener.onRequested(new EmailService.VerificationMailRequested("user@sch.ac.kr", "123456")));

        assertTrue(ex.getMessage().contains("이메일 전송 실패"));
    }
}