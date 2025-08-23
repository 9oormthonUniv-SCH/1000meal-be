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

    /** 관리자 로그인 (내부 전용; 프론트는 /auth/login 사용하는 것을 권장) */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminEntity admin = adminService.authenticate(request.getUsername(), request.getPassword());

        // ✅ 통합 토큰 발급
        AuthPrincipal principal = new AuthPrincipal(
                admin.getId(),
                admin.getUsername(),   // account
                admin.getName(),
                null,                  // 관리자 이메일 없으면 null
                "ADMIN"
        );
        String token = jwtProvider.createToken(principal);

        // 기존 스펙 유지(필요 시 LoginResponse로 통합 가능)
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
        // JwtAuthenticationFilter에서 setAuthentication(principal=account, authorities=role)로 세팅했다고 가정
        String username = authentication.getName();  // = claims.account
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