package com._1000meal.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.base64}")
    private String serviceAccountBase64;

    @PostConstruct
    public void initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return; // 이미 초기화됨
            }

            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
            ByteArrayInputStream serviceAccount =
                    new ByteArrayInputStream(decoded);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            throw new IllegalStateException("Firebase 초기화 실패", e);
        }
    }
}