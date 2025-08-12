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
        // 0) 이메일 정규화(앞뒤 공백 제거 + 소문자)
        final String email = req.getEmail().trim().toLowerCase();
        final String userId = req.getUserId().trim();

        // 1) 이메일 인증 완료 필수
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

        // 3) 비밀번호 암호화 후 저장 (Role은 엔티티 팩토리에서 STUDENT로 자동 설정)
        String encodedPw = passwordEncoder.encode(req.getPassword());
        User user = User.create(userId, encodedPw, req.getName(), email);
        userRepository.save(user);

        // (선택) 이메일 토큰 정리 필요 시: emailService.cleanUpTokens(email);

        return user.getId();
    }
}