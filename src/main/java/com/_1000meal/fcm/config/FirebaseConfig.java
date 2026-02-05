package com._1000meal.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.service-account-base64:}")
    private String serviceAccountBase64;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            // 이미 초기화된 FirebaseApp이 있으면 재사용
            List<FirebaseApp> apps = FirebaseApp.getApps();
            if (apps != null && !apps.isEmpty()) {
                log.info("[FCM] FirebaseApp already initialized.");
                return apps.get(0);
            }

            if (serviceAccountBase64 == null || serviceAccountBase64.isBlank()) {
                log.warn("[FCM] service-account-base64 is empty. Firebase initialization skipped.");
                return null;
            }

            byte[] decoded = Base64.getDecoder().decode(
                    serviceAccountBase64.getBytes(StandardCharsets.UTF_8)
            );

            try (ByteArrayInputStream is = new ByteArrayInputStream(decoded)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("[FCM] FirebaseApp initialized successfully.");
                return app;
            }

        } catch (Exception e) {
            log.error("[FCM] Firebase initialization failed", e);
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }
}