package com._1000meal.fcm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 FCM 설정 조회 응답")
public record FcmSettingResponse(
        @Schema(description = "알림 활성화 여부", example = "true")
        boolean enabled,

        @Schema(description = "FCM 토큰 등록 여부", example = "true")
        boolean hasToken
) {}