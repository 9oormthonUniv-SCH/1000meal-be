package com._1000meal.email.common;

import org.springframework.stereotype.Component;

@Component
public class ConsoleMailSender implements MailSender {

    @Override
    public void send(String to, String subject, String htmlBody) {
        System.out.println("==== 이메일 발송 ====");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + htmlBody);
        System.out.println("====================");
    }
}