package com._1000meal.user.signup.service;

import com._1000meal.email.service.EmailService;
import com._1000meal.user.domain.User;
import com._1000meal.user.repository.UserRepository;
import com._1000meal.user.signup.dto.UserSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignupService {

    private final EmailService emailService;      // user → email
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(UserSignupRequest req) {
        // 1) 이메일 인증 완료 필수
        if (!emailService.isEmailVerified(req.getEmail())) {
            throw new IllegalStateException("이메일 인증을 완료해주세요.");
        }

        // 2) 도메인/중복 체크
        if (!req.getEmail().endsWith("@sch.ac.kr"))
            throw new IllegalArgumentException("순천향대학교 이메일만 가입할 수 있습니다.");
        if (userRepository.existsByUserId(req.getUserId()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");

        // 3) 저장
        String encoded = passwordEncoder.encode(req.getPassword());
        User user = User.create(req.getUserId(), encoded, req.getName(), req.getEmail());
        userRepository.save(user);
        return user.getId();
    }
}