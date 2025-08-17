package com._1000meal.global.security;

import com._1000meal.admin.login.entity.AdminEntity;
import com._1000meal.admin.login.repository.AdminRepository;
import com._1000meal.user.domain.User;
import com._1000meal.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtProvider jwtProvider,
                                   AdminRepository adminRepository,
                                   UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 이미 인증된 요청이면 패스
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtProvider.validateToken(token)) {
                    Claims claims = jwtProvider.parse(token);
                    String subject = claims.getSubject(); // "USER" or "ADMIN"

                    if ("USER".equals(subject)) {
                        // 토큰에서 필요한 정보 추출
                        String userId = claims.get("userId", String.class); // 학번/로그인 아이디
                        String role = claims.get("role", String.class);     // e.g. STUDENT

                        // (선택) DB 존재 확인 – 필요 없다면 주석 처리 가능
                        User user = userRepository.findByUserId(userId).orElse(null);
                        if (user != null) {
                            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                            var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }

                    } else if ("ADMIN".equals(subject)) {
                        Long adminId = claims.get("adminId", Number.class).longValue();
                        String role = claims.get("role", String.class); // "ADMIN" 넣어두었으면 그대로 사용

                        // (선택) DB 조회로 유효성 체크
                        AdminEntity admin = adminRepository.findById(adminId).orElse(null);
                        if (admin != null) {
                            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                            var auth = new UsernamePasswordAuthenticationToken(admin.getUsername(), null, authorities);
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
            } catch (Exception ignored) {
                // 유효하지 않은 토큰/만료 등은 무시하고 다음 필터 진행 (익명으로 처리)
            }
        }

        filterChain.doFilter(request, response);
    }
}
