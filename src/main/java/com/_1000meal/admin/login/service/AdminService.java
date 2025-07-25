package com._1000meal.admin.login.service;

import com._1000meal.admin.login.entity.AdminEntity;
import com._1000meal.admin.login.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminEntity authenticate(String username, String rawPassword) {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자 계정입니다."));

        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return admin;
    }
}