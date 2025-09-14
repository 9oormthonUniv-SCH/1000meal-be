package com._1000meal.global.config;

import com._1000meal.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 1) 화이트리스트에서 아래 2가지만 남겨두세요(예: 인증/비밀번호/헬스/루트 등)
    //   - ★ Swagger 경로/공개 API 경로는 화이트리스트에서 제거(아래 authorize 규칙으로 처리)
    private static final String[] AUTH_WHITELIST = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/email/**",
            "/api/v1/auth/password/reset/**",
            "/api/v1/auth/find-id",
            "/api/v1/signup/user/validate-id",
            "/actuator/health",
            "/",
            "/favicon.ico",
            "/error"
    };

    // 2) 공개 조회 허용 대상 (GET만 permitAll, 그 외 메서드는 ADMIN)
    private static final String[] PUBLIC_GET_API = {
            "/api/v1/stores/**",
            "/api/v1/menus/**",
            "/api/v1/favorites/**",
            "/file",
            "/api/v1/notices/**"
    };

    // 3) Swagger 전면 차단
    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml"
    };

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // 선택: 전체 actuator 무시(보안상 운영에선 최소화 권장)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/actuator/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 기본 설정
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                // 폼/베이직 비활성화 (리다이렉트 방지)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 권한 매핑
                .authorizeHttpRequests(auth -> auth
                        // CORS 프리플라이트 통과
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger 문서 전면 차단
                        .requestMatchers(SWAGGER_PATHS).permitAll()

                        // 인증/기타 화이트리스트 허용
                        .requestMatchers(AUTH_WHITELIST).permitAll()

                        // 공개 API: GET만 허용
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_API).permitAll()
                        // 같은 경로의 비-GET은 ADMIN만
                        .requestMatchers(PUBLIC_GET_API).hasRole("ADMIN")

                        // 관리자 전용 API (예시)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 예외 처리(JSON)
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