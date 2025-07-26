package com._1000meal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (REST API에 적합)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // ✅ 모든 요청 허용
                );

        return http.build();
    }
}
