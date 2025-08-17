package com._1000meal.user.signup.service;

import com._1000meal.email.service.EmailService;
import com._1000meal.user.domain.User;
import com._1000meal.user.repository.UserRepository;
import com._1000meal.user.signup.dto.UserSignupRequest;
import com._1000meal.user.signup.dto.UserSignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignupService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponse signup(UserSignupRequest req) {
        // 0) 이메일/아이디 정규화
        final String email = req.getEmail().trim().toLowerCase();
        final String userId = req.getUserId().trim();

        // 1) 이메일 인증 필수
        if (!emailService.isEmailVerified(email)) {
            throw new IllegalStateException("이메일 인증을 완료해주세요.");
        }

        // 2) 도메인/중복 체크
        if (!email.endsWith("@sch.ac.kr")) {
            throw new IllegalArgumentException("순천향대학교 이메일만 가입할 수 있습니다.");
        }
        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 3) 저장 (Role은 User.create 내부에서 STUDENT로 설정)
        String encodedPw = passwordEncoder.encode(req.getPassword());
        User user = User.create(userId, encodedPw, req.getName(), email);
        userRepository.save(user);

        // 4) 응답 DTO
        return UserSignupResponse.from(user);
    }
}
