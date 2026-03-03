package com._1000meal.fcm.sender;

import java.util.List;

public record FcmSendResult(int successCount, int failureCount, List<FcmSendFailure> failures) {
    public FcmSendResult {
        failures = (failures == null) ? List.of() : List.copyOf(failures);
    }
}
