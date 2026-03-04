package com._1000meal.global.config;

import com._1000meal.Main;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.ZoneId;
import java.util.Arrays;

@Slf4j
@Component
public class StartupInfoLogger {

    private final Environment environment;

    public StartupInfoLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length == 0 ? "default" : Arrays.toString(activeProfiles);
        String version = Main.class.getPackage().getImplementationVersion();
        String appVersion = version != null ? version : environment.getProperty("APP_VERSION", "unknown");
        String gitSha = environment.getProperty("GIT_SHA", "unknown");

        log.info("[APP][STARTUP] version={}, gitSha={}, profiles={}, timezone={}, fcmEnabled={}",
                appVersion,
                gitSha,
                profiles,
                ZoneId.systemDefault(),
                environment.getProperty("fcm.enabled", "unknown"));
    }
}
