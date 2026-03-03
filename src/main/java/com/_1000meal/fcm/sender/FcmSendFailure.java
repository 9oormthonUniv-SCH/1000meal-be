package com._1000meal.fcm.sender;

public record FcmSendFailure(
        String token,
        String messagingErrorCode,
        String errorCode,
        String message
) {
}
