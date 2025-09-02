package com._1000meal.auth.controller;


import com._1000meal.auth.dto.ChangePasswordRequest;
import com._1000meal.auth.dto.PasswordResetConfirmRequest;
import com._1000meal.auth.dto.PasswordResetRequest;
import com._1000meal.auth.service.AdminPasswordService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/password")
@RequiredArgsConstructor
public class AdminPasswordController {

    private final AdminPasswordService service;

    /** 로그인 상태에서 변경 */
    @PatchMapping("/me")
    public ApiResponse<String> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        service.changePassword(authentication, req);
        return ApiResponse.success("비밀번호가 변경되었습니다.", SuccessCode.UPDATED);
    }

    /** (비로그인) 재설정 토큰 요청 */
    @PostMapping("/reset/request")
    public ApiResponse<String> requestReset(@Valid @RequestBody PasswordResetRequest req) {
        String token = service.requestReset(req);
        // 실제 서비스에서는 토큰을 응답으로 직접 주지 말고 이메일로만 발송 권장.
        return ApiResponse.success("비밀번호 재설정 메일(토큰)이 발송되었습니다.", SuccessCode.OK);
    }

    /** (비로그인) 토큰으로 재설정 */
    @PostMapping("/reset/confirm")
    public ApiResponse<String> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest req) {
        service.confirmReset(req);
        return ApiResponse.success("비밀번호가 재설정되었습니다.", SuccessCode.UPDATED);
    }
}