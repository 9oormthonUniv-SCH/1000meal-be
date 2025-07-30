package com._1000meal.admin.login.controller;

import com._1000meal.admin.login.dto.*;
import com._1000meal.admin.login.entity.AdminEntity;
import com._1000meal.global.security.JwtProvider;
import com._1000meal.admin.login.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminEntity admin = adminService.authenticate(request.getUsername(), request.getPassword());
        String token = jwtProvider.createToken(admin.getId(), admin.getUsername());
        return ResponseEntity.ok(new AdminLoginResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AdminSignupRequest request) {
        adminService.signup(request);
        return ResponseEntity.ok("관리자 회원가입 완료");
    }


    @GetMapping("/me")
    public ResponseEntity<AdminResponse> getMyInfo(Authentication authentication) {
        // JWT 필터에서 authentication.getName() == username 이 됨
        String username = authentication.getName();
        AdminEntity admin = adminService.getAdminByUsername(username);

        // 응답 DTO 생성 (id, username 등 민감하지 않은 정보만)
        AdminResponse response = new AdminResponse(admin.getId(), admin.getUsername(), admin.getName(), admin.getPhoneNumber());
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody PasswordChangeRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        adminService.changePassword(username, request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}