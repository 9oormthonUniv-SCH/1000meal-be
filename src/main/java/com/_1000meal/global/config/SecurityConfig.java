package com._1000meal.global.config;

import com._1000meal.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 필요 시 수정: 화이트리스트
    private static final String[] AUTH_WHITELIST = {
            // 통합 인증
            "/auth/login",
            "/auth/signup",
            "/auth/email/**",
            // 과도기/관리자
            "/api/admin/login",
            "/api/admin/signup",
            // 공개 API
            "/api/v1/stores/**",
            "/api/v1/menus/**",
            // 문서/헬스체크
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/actuator/health",
            // 정적/루트
            "/",
            "/favicon.ico",
            "/error"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 기본 세팅
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                // 폼로그인/베이직 인증 비활성화 (리다이렉트 방지 핵심)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 권한 매핑
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )

                // JWT 필터 (UsernamePasswordAuthenticationFilter 앞)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 예외 처리(JSON 일관 응답)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("""
            {
              "data": null,
              "result": {
                "code": "AUTH_401",
                "message": "인증이 필요합니다.",
                "timestamp": "%s"
              }
            }
            """.formatted(LocalDateTime.now()));
        };
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("""
            {
              "data": null,
              "result": {
                "code": "AUTH_403",
                "message": "접근이 거부되었습니다.",
                "timestamp": "%s"
              }
            }
            """.formatted(LocalDateTime.now()));
        };
    }
}