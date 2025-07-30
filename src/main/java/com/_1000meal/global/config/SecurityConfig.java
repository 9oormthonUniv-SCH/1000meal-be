package com._1000meal.global.config;

import com._1000meal.global.security.JwtAuthenticationFilter;
import com._1000meal.user.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ======== 관리자 관련 API ========
                        .requestMatchers("/api/admin/signup", "/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        // ======== 사용자 관련 API ========
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        // Swagger 문서 허용 (springdoc-openapi 기준)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        // ======== 기타 API ========
                        .anyRequest().authenticated()
                )
                // ======== OAuth2 로그인 (사용자) ========
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/login/success", true)
                )
                // ======== JWT 인증 필터 (관리자) ========
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}