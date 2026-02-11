package com._1000meal.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Profile({"prod", "dev-firebase"})
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${fcm.service-account-base64:}")
    private String serviceAccountBase64;

    @PostConstruct
    public void init() throws Exception {
        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (apps != null && !apps.isEmpty()) {
            log.info("[FCM] FirebaseApp already initialized.");
            return;
        }

        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            initializeFromJson(serviceAccountJson);
            return;
        }

        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            String json = new String(
                    Base64.getDecoder().decode(serviceAccountBase64.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8
            );
            initializeFromJson(json);
            return;
        }

        throw new IllegalStateException("firebase.service-account-json is empty");
    }

    private void initializeFromJson(String json) throws Exception {
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(is))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("[FCM] FirebaseApp initialized successfully.");
        }
    }
}
