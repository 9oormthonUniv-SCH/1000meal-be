package com._1000meal.admin.login.controller;

import com._1000meal.admin.login.dto.*;
import com._1000meal.admin.login.entity.AdminEntity;
import com._1000meal.admin.login.service.AdminService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.global.security.JwtProvider;
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
    public ApiResponse<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminEntity admin = adminService.authenticate(request.getUsername(), request.getPassword());

        // ★ 관리자 전용 토큰
        String token = jwtProvider.createAdminToken(admin.getId());

        AdminLoginResponse body = new AdminLoginResponse(
                token,
                admin.getId(),
                admin.getUsername(),
                admin.getName(),
                admin.getPhoneNumber()
        );
        return ApiResponse.ok(body);
    }

    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody AdminSignupRequest request) {
        adminService.signup(request);
        return ApiResponse.success("관리자 회원가입 완료", SuccessCode.CREATED);
    }

    @GetMapping("/me")
    public ApiResponse<AdminResponse> getMyInfo(Authentication authentication) {
        // JwtAuthenticationFilter에서 username을 principal로 세팅했다는 가정
        String username = authentication.getName();
        AdminEntity admin = adminService.getAdminByUsername(username);

        AdminResponse response = new AdminResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getName(),
                admin.getPhoneNumber()
        );
        return ApiResponse.ok(response);
    }

    @PatchMapping("/password")
    public ApiResponse<String> changePassword(
            @RequestBody PasswordChangeRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        adminService.changePassword(username, request);
        return ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", SuccessCode.UPDATED);
    }
}
