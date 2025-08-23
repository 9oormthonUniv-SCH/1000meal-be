package com._1000meal.adminlogin.service;

import com._1000meal.adminlogin.dto.AdminSignupRequest;
import com._1000meal.adminlogin.dto.PasswordChangeRequest;
import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.adminlogin.repository.AdminRepository;
import com._1000meal.global.error.code.AdminSignupErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.util.PasswordValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminEntity signup(AdminSignupRequest req) {
        // 1. 아이디(Username) 중복 체크
        if (adminRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new CustomException(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS);
        }

        // 3. 필수 정보 누락 체크
        if (req.getUsername() == null || req.getPassword() == null || req.getName() == null) {
            throw new CustomException(AdminSignupErrorCode.REQUIRED_FIELD_MISSING);
        }
        // 4. 비밀번호 강도 체크 (예시)
        if (req.getPassword().length() < 8) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }

        PasswordValidator.validatePassword(req.getPassword(), req.getUsername(), req.getPhoneNumber());

        // 5. 비밀번호 암호화 후 저장
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
                .orElseThrow(() -> new CustomException(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS)); // 로그인/조회용 에러코드는 분리 가능

        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_MISMATCH); // 로그인용 에러코드로 분리 권장
        }

        return admin;
    }

    //조회
    public AdminEntity getAdminByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS)); // 마찬가지로 조회용 에러코드 분리 가능
    }

    //비밀번호 수정
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS));
        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_MISMATCH);
        }
        // 새 비밀번호 암호화 & 저장
        admin.changePassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);
    }
}