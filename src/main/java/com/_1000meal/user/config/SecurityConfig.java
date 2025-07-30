package com._1000meal.user.config;

import com._1000meal.user.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // REST API 기반이라면 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll() // 공개 접근
                        .anyRequest().authenticated() // 그 외는 인증 필요
                )
                .oauth2Login(oauth -> oauth
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService) // OAuth2 로그인 성공 후 사용자 정보 처리
                                )
                        // 로그인 성공 후 처리를 위한 successHandler 또는 redirect 설정은 이후 단계에서 추가 가능
                        .defaultSuccessUrl("/login/success", true) // 여기 추가!
                );

        return http.build();
    }
}