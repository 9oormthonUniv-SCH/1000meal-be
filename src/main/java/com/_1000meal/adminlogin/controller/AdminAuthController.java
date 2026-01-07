package com._1000meal.adminlogin.controller;

import com._1000meal.adminlogin.dto.AdminLoginRequest;
import com._1000meal.adminlogin.dto.AdminLoginResponse;
import com._1000meal.adminlogin.dto.AdminResponse;
import com._1000meal.adminlogin.dto.AdminSignupRequest;
import com._1000meal.adminlogin.dto.PasswordChangeRequest;
import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.adminlogin.service.AdminService;

import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.global.security.JwtProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Auth", description = "관리자 인증 / 계정 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtProvider jwtProvider;

    @Operation(
            summary = "관리자 로그인",
            description = "아이디와 비밀번호로 관리자 로그인을 수행하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "관리자 계정 없음")
    })
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(
            @RequestBody AdminLoginRequest request
    ) {
        AdminEntity admin = adminService.authenticate(
                request.getUsername(),
                request.getPassword()
        );

        AuthPrincipal principal = new AuthPrincipal(
                admin.getId(),
                admin.getUsername(),
                admin.getName(),
                null,
                "ADMIN"
        );

        String token = jwtProvider.createToken(principal);

        AdminLoginResponse response = new AdminLoginResponse(
                token,
                admin.getId(),
                admin.getUsername(),
                admin.getName(),
                admin.getPhoneNumber()
        );

        return ApiResponse.ok(response);
    }

    @Operation(
            summary = "관리자 회원가입",
            description = "새로운 관리자 계정을 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수값 누락 또는 비밀번호 규칙 위반"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 관리자")
    })
    @PostMapping("/signup")
    public ApiResponse<String> signup(
            @RequestBody AdminSignupRequest request
    ) {
        adminService.signup(request);
        return ApiResponse.success("관리자 회원가입 완료", SuccessCode.CREATED);
    }

    @Operation(
            summary = "내 관리자 정보 조회",
            description = "현재 로그인된 관리자의 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me")
    public ApiResponse<AdminResponse> getMyInfo(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
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

    @Operation(
            summary = "관리자 비밀번호 변경",
            description = "기존 비밀번호를 확인한 후 새 비밀번호로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "기존 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "비밀번호 규칙 위반")
    })
    @PatchMapping("/password")
    public ApiResponse<String> changePassword(
            @RequestBody PasswordChangeRequest request,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        String username = authentication.getName();
        adminService.changePassword(username, request);
        return ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", SuccessCode.UPDATED);
    }
}