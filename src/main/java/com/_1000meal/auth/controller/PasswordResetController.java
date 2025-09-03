package com._1000meal.auth.controller;

import com._1000meal.auth.dto.PasswordResetConfirmRequest;
import com._1000meal.auth.dto.PasswordResetRequest;
import com._1000meal.auth.dto.SimpleMessageResponse;
import com._1000meal.auth.service.PasswordResetService;
import com._1000meal.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // 비로그인: 재설정 링크 요청
    @PostMapping("/request")
    public ApiResponse<SimpleMessageResponse> request(@RequestBody @Valid PasswordResetRequest req) {
        passwordResetService.requestReset(req);
        return ApiResponse.ok(new SimpleMessageResponse("재설정 링크를 이메일로 보냈습니다."));
    }

    // 비로그인: 새 비밀번호 설정
    @PostMapping("/confirm")
    public ApiResponse<SimpleMessageResponse> confirm(@RequestBody @Valid PasswordResetConfirmRequest req) {
        passwordResetService.confirmReset(req);
        return ApiResponse.ok(new SimpleMessageResponse("비밀번호가 재설정되었습니다."));
    }
}