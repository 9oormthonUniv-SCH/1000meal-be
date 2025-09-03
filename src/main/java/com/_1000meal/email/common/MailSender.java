package com._1000meal.email.common;

public interface MailSender {
    void send(String to, String subject, String htmlBody);
}