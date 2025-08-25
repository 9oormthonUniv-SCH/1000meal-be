package com._1000meal.global.config;

import com._1000meal.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
=======
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
>>>>>>> juheun/signup
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
<<<<<<< HEAD
import jakarta.servlet.http.HttpServletResponse; // 또는 javax.servlet.http.HttpServletResponse
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

=======
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
>>>>>>> juheun/signup

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
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/actuator/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 기본 세팅
                .csrf(csrf -> csrf.disable())
<<<<<<< HEAD
                .cors(Customizer.withDefaults())

                // 전부 오픈
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                        .requestMatchers("/**").permitAll()
                )

                // 로그인/세션/기본인증 전부 비활성화 (테스트용)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth -> oauth.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
=======
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
>>>>>>> juheun/signup

        return http.build();
    }

<<<<<<< HEAD
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(Customizer.withDefaults())
//                .authorizeHttpRequests(auth -> auth
//                        // ======== 관리자 관련 API ========
//                        .requestMatchers("/api/admin/signup", "/api/admin/login").permitAll()
//                        .requestMatchers("/api/admin/**").authenticated()
//                        // ======== 사용자 관련 API ========
//                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
//                        .requestMatchers("/signup/user", "/login/user").permitAll()
//                        // =======이메일 인증=====
//                        .requestMatchers("/signup/email/send").permitAll()
//                        .requestMatchers("/signup/email/verify").permitAll()
//                        .requestMatchers("/signup/email/status").permitAll()
//                        // Swagger 문서 허용 (springdoc-openapi 기준)
//                        // ======== 메뉴, 가게 관련 API ========
//                        .requestMatchers("/api/v1/stores/**").permitAll()
//                        .requestMatchers("/api/v1/menus/**").permitAll()
//                        .requestMatchers(
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/v3/api-docs/**",
//                                "/v3/api-docs.yaml"
//                        ).permitAll()
//                        // ======== 기타 API ========
//                        .anyRequest().authenticated()
//                )
//                // ======== OAuth2 로그인 (사용자) ========
//                .oauth2Login(oauth -> oauth
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService)
//                        )
//                        .defaultSuccessUrl("/login/success", true)
//                )
//                // ======== JWT 인증 필터 (관리자) ========
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//
//                // ... 기타 설정
//
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .authenticationEntryPoint(restAuthenticationEntryPoint())
//                        .accessDeniedHandler(restAccessDeniedHandler()));
//
//        // 권한실패
//        return http.build();
//    }

    private <H extends HttpSecurityBuilder<H>> ExceptionHandlingConfigurer<H> exceptionHandling() {
        return null;
    }

=======
>>>>>>> juheun/signup
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