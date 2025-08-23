// 패키지: com._1000meal.adminlogin.controller
package com._1000meal.adminlogin.controller;

import com._1000meal.adminlogin.dto.AdminLoginRequest;
import com._1000meal.adminlogin.dto.AdminLoginResponse;
import com._1000meal.adminlogin.dto.AdminResponse;
import com._1000meal.adminlogin.dto.AdminSignupRequest;
import com._1000meal.adminlogin.dto.PasswordChangeRequest;
import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.adminlogin.service.AdminService;

import com._1000meal.auth.model.AuthPrincipal;           // ✅ 통합 프린시펄
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

        AuthPrincipal principal = new AuthPrincipal(
                admin.getId(),
                admin.getUsername(),
                admin.getName(),
                null,
                "ADMIN"
        );
        String token = jwtProvider.createToken(principal);

        AdminLoginResponse body = new AdminLoginResponse(
                token,
                admin.getId(),
                admin.getUsername(),
                admin.getName(),
                admin.getPhoneNumber()
        );
        // 200 OK
        return ApiResponse.ok(body);
    }

    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody AdminSignupRequest request) {
        adminService.signup(request);
        // 201 Created 등 커스텀 성공코드 사용
        return ApiResponse.success("관리자 회원가입 완료", SuccessCode.CREATED);
    }

    @GetMapping("/me")
    public ApiResponse<AdminResponse> getMyInfo(Authentication authentication) {
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