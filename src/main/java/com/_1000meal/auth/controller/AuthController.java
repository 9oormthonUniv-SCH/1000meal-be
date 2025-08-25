package com._1000meal.auth.controller;

import com._1000meal.auth.dto.LoginRequest;
import com._1000meal.auth.dto.LoginResponse;
import com._1000meal.auth.dto.SignupRequest;
import com._1000meal.auth.dto.SignupResponse;
import com._1000meal.auth.service.AuthService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "통합 회원가입/로그인 API")
@RestController
@RequestMapping(value = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "통합 회원가입", description = "role, userId, name, email, password로 회원가입합니다.")
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse resp = authService.signup(req);
        return ApiResponse.success(resp, SuccessCode.CREATED); // (data, code) 순서 유지
    }

    @Operation(summary = "통합 로그인", description = "usernameOrEmail, password로 로그인하고 토큰을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ApiResponse.ok(resp);
    }

    @Operation(summary = "내 정보 조회", description = "Bearer 토큰 기반으로 현재 로그인한 계정 정보를 반환합니다.")
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(Authentication authentication) {
        LoginResponse resp = authService.me(authentication);
        return ApiResponse.ok(resp);
    }
}