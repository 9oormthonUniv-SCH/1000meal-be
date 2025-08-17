package com._1000meal.user.login.service;

import com._1000meal.global.security.JwtProvider;
import com._1000meal.user.domain.User;
import com._1000meal.user.login.dto.UserLoginRequest;
import com._1000meal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;   // 리팩터링한 JwtProvider (createUserToken 사용)

    /** 로그인 성공 시 학생용 JWT 발급 */
    public String login(UserLoginRequest req) {
        final String userId = req.getUserId().trim();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // ★ 페이로드에 uid/userId/name/email/role 이 담긴 사용자 전용 토큰
        return jwtProvider.createUserToken(user);
    }
}
