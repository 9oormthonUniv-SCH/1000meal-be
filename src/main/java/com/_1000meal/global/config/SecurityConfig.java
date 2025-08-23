package com._1000meal.global.config;

import com._1000meal.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF (API 기반이면 disable)
                .csrf(csrf -> csrf.disable())

                // 세션은 사용하지 않음 (JWT)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CORS (필요 시 별도 설정)
                .cors(Customizer.withDefaults())

                // 권한 매핑
                .authorizeHttpRequests(auth -> auth
                        // ------ 공용/비인증 허용 ------
                        // 통합 로그인(또는 과도기: 기존 로그인/회원가입 허용)
                        .requestMatchers(
                                "/login",               // 통합 로그인 엔드포인트(예: /login)
                                "/login/**",            // 소셜 리다이렉트 등
                                "/signup/user",         // 과도기 – 유저 회원가입
                                "/signup/email/**"      // 이메일 인증 플로우
                        ).permitAll()

                        // 관리자 회원가입/로그인 (과도기)
                        .requestMatchers("/api/admin/signup", "/api/admin/login").permitAll()

                        // 메뉴/가게 공개 API
                        .requestMatchers("/api/v1/stores/**", "/api/v1/menus/**").permitAll()

                        // Swagger & 문서
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // 정적/기타
                        .requestMatchers("/", "/favicon.ico", "/error").permitAll()

                        // ------ 나머지는 인증 필요 ------
                        .anyRequest().authenticated()
                )

                // JWT 필터 (UsernamePasswordAuthenticationFilter 앞)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 예외 처리(JSON 응답)
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
              "statusCode": 401,
              "message": "로그인이 필요합니다.",
              "timestamp": "%s",
              "data": null
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
              "statusCode": 403,
              "message": "권한이 없습니다.",
              "timestamp": "%s",
              "data": null
            }
            """.formatted(LocalDateTime.now()));
        };
    }
}