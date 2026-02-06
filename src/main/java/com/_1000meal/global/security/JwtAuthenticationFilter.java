package com._1000meal.global.security;

import com._1000meal.auth.model.AuthPrincipal;
//import com._1000meal.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    //private final UserRepository userRepository;

    /** SecurityConfig와 반드시 동일하게 유지 */
    private static final String[] WHITELIST = {
            "/auth/login",
            "/auth/signup",
            "/auth/email/**",
            "/api/admin/login",
            "/api/admin/signup",
            "/api/v1/stores/**",
            "/api/v1/menus/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/actuator/health",
            "/",
            "/favicon.ico",
            "/error",
    };

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private boolean isWhitelisted(HttpServletRequest req) {
        String uri = req.getRequestURI();
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, uri)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // 0) 화이트리스트는 무조건 통과
//        if (isWhitelisted(request)) {
//            chain.doFilter(request, response);
//            return;
//        }

        // 1) 이미 인증된 요청이면 패스
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Authorization 헤더 확인 (없으면 그대로 체인 진행)
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtProvider.validate(token)) {
                // 유효하지 않으면 인증 없이 계속 진행 (최종 401은 Security가 처리)
                chain.doFilter(request, response);
                return;
            }

            // 3) 토큰 파싱 → Claims → AuthPrincipal 구성
            Claims claims = jwtProvider.parse(token);

            Number idNum = claims.get("id", Number.class);
            Long id = (idNum != null) ? idNum.longValue() : null;

            String account = claims.get("account", String.class); // username(관리자) / 학번(학생)
            String role = claims.get("role", String.class);       // "STUDENT" | "ADMIN"
            String name = claims.get("name", String.class);
            String email = claims.get("email", String.class);

            if (role == null || account == null) {
                // 필수 클레임 없으면 인증 생략
                chain.doFilter(request, response);
                return;
            }

            // (선택) 레거시 DB 존재 확인이 필요하면 주석 해제
            /*
            boolean exists = true;
            if ("ADMIN".equals(role)) {
                exists = (id != null) ? adminRepository.findById(id).isPresent()
                                      : adminRepository.findByUsername(account).isPresent();
            } else { // STUDENT
                exists = (id != null) ? userRepository.findById(id).isPresent()
                                      : userRepository.findByUserId(account).isPresent();
            }
            if (!exists) {
                chain.doFilter(request, response);
                return;
            }
            */

            AuthPrincipal principal = new AuthPrincipal(
                    id,
                    account,
                    name,
                    email,
                    role
            );

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ignored) {
            // 만료/서명 오류 등: 여기서 직접 401을 쓰지 말고 익명으로 계속 진행
            // (최종 401/403은 SecurityConfig의 exceptionHandling이 책임짐)
        }

        chain.doFilter(request, response);
    }
}
