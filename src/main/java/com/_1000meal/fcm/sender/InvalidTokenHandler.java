package com._1000meal.fcm.sender;

import java.util.List;

public interface InvalidTokenHandler {
    void handleInvalidTokens(List<String> invalidTokens);
}
