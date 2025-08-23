package com._1000meal.global.security;

import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.adminlogin.repository.AdminRepository;
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

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            AdminRepository adminRepository,
            UserRepository userRepository
    ) {
        this.jwtProvider = jwtProvider;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증된 요청이면 패스
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtProvider.validate(token)) {
                    Claims claims = jwtProvider.parse(token);

                    // 통합 스키마 기준으로 읽기
                    String subject = claims.getSubject();             // "USER" or "ADMIN"
                    String role     = claims.get("role", String.class);    // e.g. "STUDENT" or "ADMIN"
                    String account  = claims.get("account", String.class); // userId or admin username

                    // id는 Number로 들어올 수 있으니 안전 변환
                    Number idNum = claims.get("id", Number.class);
                    Long id = (idNum != null) ? idNum.longValue() : null;

                    if (role == null || account == null) {
                        // 필수 클레임 없으면 인증 생략
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // 권한 부여
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    if ("USER".equals(subject)) {
                        // 필요하면 DB 확인(없어도 토큰 신뢰 시 바로 세팅 가능)
                        boolean ok = true;
                        if (id != null) {
                            ok = userRepository.findById(id).isPresent();
                        } else {
                            ok = userRepository.findByUserId(account).isPresent();
                        }

                        if (ok) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    account, // principal: 학번(userId)
                                    null,
                                    authorities
                            );
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }

                    } else if ("ADMIN".equals(subject)) {
                        boolean ok = true;
                        if (id != null) {
                            ok = adminRepository.findById(id).isPresent();
                        } else {
                            ok = adminRepository.findByUsername(account).isPresent();
                        }

                        if (ok) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    account, // principal: 관리자 username
                                    null,
                                    authorities
                            );
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
            } catch (Exception ignored) {
                // 만료/서명오류 등은 무시하고 익명으로 진행
            }
        }

        filterChain.doFilter(request, response);
    }
}