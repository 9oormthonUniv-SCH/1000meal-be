package com._1000meal.auth.controller;

import com._1000meal.auth.dto.LoginRequest;
import com._1000meal.auth.dto.LoginResponse;
import com._1000meal.auth.dto.LogoutRequest;
import com._1000meal.auth.dto.RefreshTokenRequest;
import com._1000meal.auth.dto.RefreshTokenResponse;
import com._1000meal.auth.dto.SimpleMessageResponse;
import com._1000meal.auth.dto.SignupRequest;
import com._1000meal.auth.dto.SignupResponse;
import com._1000meal.auth.refresh.RefreshTokenService;
import com._1000meal.auth.service.AuthService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "통합 회원가입/로그인/내 정보 조회 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "통합 회원가입",
            description = """
                    역할(role)에 따라 계정을 생성합니다.
                    - STUDENT: 학교 이메일 인증이 완료된 이메일로만 가입 가능
                    - ADMIN: 역할에 맞는 추가 정보가 필요할 수 있음(요구사항에 따라)
                    
                    성공 시 생성된 계정의 식별자와 기본 정보를 반환합니다.
                    """
    )
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse resp = authService.signup(req);
        return ApiResponse.success(resp, SuccessCode.CREATED);
    }

    @Operation(
            summary = "통합 로그인",
            description = """
                    계정 자격 증명을 검증한 뒤 Access Token을 발급합니다.
                    - 역할(role)과 아이디/비밀번호 조합이 유효해야 합니다.
                    - 탈퇴/비활성 상태 계정은 로그인할 수 없습니다.
                    
                    성공 시 Access Token 및 계정 기본 정보를 반환합니다.
                    """
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        LoginResponse resp = authService.login(req, request);
        return ApiResponse.ok(resp);
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "만료된 Access Token 이후 Refresh Token으로 새로운 Access Token을 발급합니다."
    )
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = refreshTokenService.refresh(request.refreshToken());
        return ApiResponse.ok(response);
    }

    @Operation(
            summary = "로그아웃",
            description = "전달된 Refresh Token을 폐기합니다. 이미 폐기/미존재여도 200을 반환합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<SimpleMessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.logout(request.refreshToken());
        return ApiResponse.ok(new SimpleMessageResponse("로그아웃 되었습니다."));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "내 정보 조회",
            description = """
                    Bearer Access Token으로 인증된 사용자(현재 로그인한 계정)의 정보를 반환합니다.
                    - Authorization: Bearer {token}
                    """
    )
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(Authentication authentication) {
        LoginResponse resp = authService.me(authentication);
        return ApiResponse.ok(resp);
    }
}
