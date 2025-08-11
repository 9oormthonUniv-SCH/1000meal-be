package com._1000meal.user.login.service;

import com._1000meal.global.security.JwtProvider;
import com._1000meal.user.domain.User;
import com._1000meal.user.repository.UserRepository;
import com._1000meal.user.login.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// JwtProvider는 프로젝트에 맞게 주입 (예: global.security.JwtTokenProvider)
@Service
@RequiredArgsConstructor
public class UserLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider; // 존재하는 유틸 사용

    public String login(UserLoginRequest req) {
        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // role=STUDENT로 토큰 발급
        return jwtProvider.createToken(user.getId(), "STUDENT");
    }
}