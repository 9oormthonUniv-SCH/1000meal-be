package com._1000meal.fcm.controller;

import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.fcm.dto.FcmTokenRegisterRequest;
import com._1000meal.fcm.dto.NotificationPreferenceResponse;
import com._1000meal.fcm.dto.NotificationPreferenceUpdateRequest;
import com._1000meal.fcm.service.FcmService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM", description = "FCM 토큰/알림 설정 관리 API (로그인 사용자 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
public class FcmController {

    private final FcmService fcmService;

    @Operation(
            summary = "FCM 토큰 등록/재연결",
            description = """
                    로그인한 사용자의 FCM 토큰을 등록합니다.

                    - 동일 토큰이 이미 존재하면 accountId를 현재 사용자로 재연결합니다.
                    - 최초 등록 시 알림 설정(NotificationPreference)은 기본 ON으로 생성됩니다.
                    """
    )
    @PostMapping("/tokens")
    public ApiResponse<Void> registerToken(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody FcmTokenRegisterRequest request
    ) {
        fcmService.registerOrRelinkToken(principal.id(), request.token(), request.platform());
        return ApiResponse.success(null, SuccessCode.OK);
    }



    @Operation(
            summary = "내 알림 설정 조회",
            description = "로그인한 사용자의 알림 활성/비활성 상태를 조회합니다."
    )
    @GetMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> getPreference(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        boolean enabled = fcmService.getEnabled(principal.id());
        return ApiResponse.success(new NotificationPreferenceResponse(enabled), SuccessCode.OK);
    }

    @Operation(
            summary = "내 알림 설정 변경",
            description = """
                    로그인한 사용자의 알림 활성/비활성 상태를 변경합니다.

                    - 설정 레코드가 없으면 기본 ON 레코드를 생성한 뒤 변경합니다.
                    """
    )
    @PatchMapping("/preferences")
    public ApiResponse<Void> updatePreference(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody NotificationPreferenceUpdateRequest request
    ) {
        fcmService.setEnabled(principal.id(), request.enabled());
        return ApiResponse.success(null, SuccessCode.UPDATED);
    }
}