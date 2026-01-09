package com._1000meal.fcm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 ON/OFF 변경 요청")
public record FcmToggleRequest(
        @Schema(description = "알림 활성화 여부", example = "true")
        boolean enabled
) {}