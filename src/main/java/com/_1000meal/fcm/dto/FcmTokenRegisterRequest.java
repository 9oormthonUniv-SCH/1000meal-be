package com._1000meal.fcm.dto;

import com._1000meal.fcm.domain.FcmPlatform;

public record FcmTokenRegisterRequest(
        String token,
        FcmPlatform platform
) {}