package com._1000meal.fcm.controller;

import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.fcm.dto.FcmSettingResponse;
import com._1000meal.fcm.dto.FcmToggleRequest;
import com._1000meal.fcm.service.FcmSettingService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM", description = "FCM 알림 설정 API")
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmSettingService fcmSettingService;

    @Operation(summary = "내 FCM 알림 설정 조회", description = "로그인한 사용자의 알림 활성화 여부/토큰 등록 여부를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public ApiResponse<FcmSettingResponse> getMySetting(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        FcmSettingResponse resp = fcmSettingService.getMySetting(principal.id());
        return ApiResponse.success(resp, SuccessCode.OK);
    }

    @Operation(summary = "FCM 알림 ON/OFF", description = "로그인한 사용자의 알림 설정을 활성/비활성화 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PatchMapping("/me")
    public ApiResponse<Void> toggle(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody FcmToggleRequest request
    ) {
        fcmSettingService.toggle(principal.id(), request.enabled());
        return ApiResponse.success(null, SuccessCode.UPDATED);
    }
}