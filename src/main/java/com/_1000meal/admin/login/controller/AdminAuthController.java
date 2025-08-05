package com._1000meal.admin.login.controller;

import com._1000meal.admin.login.dto.*;
import com._1000meal.admin.login.entity.AdminEntity;
import com._1000meal.global.security.JwtProvider;
import com._1000meal.admin.login.service.AdminService;
import com._1000meal.global.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminEntity admin = adminService.authenticate(request.getUsername(), request.getPassword());
        String token = jwtProvider.createToken(admin.getId(), admin.getUsername());
        return Result.ok(new AdminLoginResponse(token));
    }

    @PostMapping("/signup")
    public Result<String> signup(@RequestBody AdminSignupRequest request) {
        adminService.signup(request);
        return Result.ok("관리자 회원가입 완료");
    }

    @GetMapping("/me")
    public Result<AdminResponse> getMyInfo(Authentication authentication) {
        // JWT 필터에서 authentication.getName() == username 이 됨
        String username = authentication.getName();
        AdminEntity admin = adminService.getAdminByUsername(username);

        // 응답 DTO 생성 (id, username 등 민감하지 않은 정보만)
        AdminResponse response = new AdminResponse(admin.getId(), admin.getUsername(), admin.getName(), admin.getPhoneNumber());
        return Result.ok(response);
    }

    @PatchMapping("/password")
    public Result<String> changePassword(
            @RequestBody PasswordChangeRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        adminService.changePassword(username, request);
        return Result.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}