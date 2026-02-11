package com._1000meal.fcm.debug;

import com._1000meal.fcm.service.OpenNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// src/main/java/com/_1000meal/fcm/debug/OpenNotificationDebugRunner.java
@Profile("fcm-debug")
@Component
@RequiredArgsConstructor
public class OpenNotificationDebugRunner implements CommandLineRunner {

    private final OpenNotificationService openNotificationService;

    @Override
    public void run(String... args) {
        // 테스트용 날짜 (평일로!)
        LocalDate testDate = LocalDate.now();

        openNotificationService.sendDailyOpenNotifications(testDate);
    }
}