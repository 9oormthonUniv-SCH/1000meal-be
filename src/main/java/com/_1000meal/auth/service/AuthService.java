// com._1000meal.auth.service.AuthService
package com._1000meal.auth.service;

import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.adminlogin.repository.AdminRepository;
import com._1000meal.auth.dto.LoginRequest;
import com._1000meal.auth.dto.LoginResponse;
import com._1000meal.auth.model.AuthPrincipal;      // ✅ 공통 프린시펄
import com._1000meal.global.security.JwtProvider;
import com._1000meal.user.domain.User;
import com._1000meal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest req) {
        final String input = req.getUsername().trim();

        // 1) 학생 로그인 (학번 userId 기준)
        User user = userRepository.findByUserId(input).orElse(null);
        if (user != null) {
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
            }

            AuthPrincipal principal = new AuthPrincipal(
                    user.getId(),
                    user.getUserId(),        // account
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name()    // "STUDENT"
            );
            String token = jwtProvider.createToken(principal);

            return new LoginResponse(
                    token,
                    principal.role(),        // "STUDENT"
                    principal.account(),     // 학번(userId)
                    principal.name(),
                    principal.email()
            );
        }

        // 2) 관리자 로그인 (username 기준)
        AdminEntity admin = adminRepository.findByUsername(input).orElse(null);
        if (admin != null) {
            if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
                throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
            }

            AuthPrincipal principal = new AuthPrincipal(
                    admin.getId(),
                    admin.getUsername(),     // account
                    admin.getName(),
                    null,                    // 관리자 이메일 없으면 null
                    "ADMIN"
            );
            String token = jwtProvider.createToken(principal);

            return new LoginResponse(
                    token,
                    principal.role(),        // "ADMIN"
                    principal.account(),     // 관리자 username
                    principal.name(),
                    principal.email()        // null 가능
            );
        }

        // (선택) 학생 이메일로 로그인 허용하려면 아래 로직을 추가해도 됨:
        // User byEmail = userRepository.findByEmail(input.toLowerCase()).orElse(null);
        // ...

        throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}