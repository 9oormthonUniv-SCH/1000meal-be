package com._1000meal.fcm.sender;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoopInvalidTokenHandler implements InvalidTokenHandler {
    @Override
    public void handleInvalidTokens(List<String> invalidTokens) {
        // no-op
    }
}
