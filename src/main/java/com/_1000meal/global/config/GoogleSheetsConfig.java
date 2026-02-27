package com._1000meal.global.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Google Sheets API 클라이언트 설정.
 *
 * sheets.enabled=true 인 환경에서만 활성화된다.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "sheets.enabled", havingValue = "true")
public class GoogleSheetsConfig {

    @Value("${sheets.service-account-json-path:1000meal_sheets_service_account.json}")
    private String serviceAccountJsonPath;

    @Value("${sheets.application-name:1000meal-roster-export}")
    private String applicationName;

    @Bean
    public Sheets sheetsService() throws Exception {
        Path path = Path.of(serviceAccountJsonPath).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IllegalStateException("Google Sheets service account JSON not found: " + path);
        }

        try (InputStream is = Files.newInputStream(path)) {
            var credentials = ServiceAccountCredentials.fromStream(is)
                    .createScoped("https://www.googleapis.com/auth/spreadsheets");

            log.info("[Sheets] Initializing Sheets client with service account json at {}", path);

            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName(applicationName).build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Google Sheets service account JSON: " + path, e);
        }
    }
}

