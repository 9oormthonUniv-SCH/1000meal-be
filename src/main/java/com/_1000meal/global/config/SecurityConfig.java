package com._1000meal.global.config;

import com._1000meal.global.security.JwtAuthenticationFilter;
import com._1000meal.userOauth.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse; // 또는 javax.servlet.http.HttpServletResponse
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;


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

        return http.build();
    }

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

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");  // ← 여기!
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
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
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