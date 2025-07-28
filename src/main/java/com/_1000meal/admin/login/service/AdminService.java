package com._1000meal.admin.login.service;

import com._1000meal.admin.login.dto.AdminSignupRequest;
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

    public AdminEntity signup(AdminSignupRequest req) {
        // 아이디 중복 체크
        if (adminRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        // 비밀번호 암호화 후 저장
        AdminEntity admin = AdminEntity.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .phoneNumber(req.getPhoneNumber())
                .build();

        return adminRepository.save(admin);
    }

    public AdminEntity authenticate(String username, String rawPassword) {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자 계정입니다."));

        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return admin;
    }

    //조회
    public AdminEntity getAdminByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("관리자 계정이 존재하지 않습니다."));
    }
}