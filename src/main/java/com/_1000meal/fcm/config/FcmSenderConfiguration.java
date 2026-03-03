package com._1000meal.fcm.config;

import com._1000meal.fcm.sender.FcmSender;
import com._1000meal.fcm.sender.FirebaseFcmSender;
import com._1000meal.fcm.sender.NoopFcmSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmSenderConfiguration {

    @Bean
    @ConditionalOnProperty(name = "fcm.enabled", havingValue = "true")
    public FcmSender firebaseFcmSender() {
        return new FirebaseFcmSender();
    }

    @Bean
    @ConditionalOnMissingBean(FcmSender.class)
    public FcmSender noopFcmSender() {
        return new NoopFcmSender();
    }
}
