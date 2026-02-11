package com._1000meal.fcm.message;

import com._1000meal.fcm.domain.NotificationType;

public final class FcmMessageFactory {

    private static final boolean APPEND_STORE_NAME_IN_BODY = false;

    private FcmMessageFactory() {
    }

    public static FcmMessage of(NotificationType type, String storeName, String groupName, int remain) {
        String safeGroupName = (groupName == null || groupName.isBlank()) ? storeName : groupName;

        return switch (type) {
            case LOW_STOCK_30 -> new FcmMessage(
                    "[" + safeGroupName + "] 마감 임박!",
                    withOptionalStoreName("천원의 아침밥 수량이 30개 이하로 줄었어요!", storeName)
            );
            case LOW_STOCK_10 -> new FcmMessage(
                    "[" + safeGroupName + "] 마감 직전!",
                    withOptionalStoreName("천원의 아침밥 수량이 10개 이하로 줄었어요!", storeName)
            );
            case STOCK_DEADLINE -> new FcmMessage(
                    "[" + safeGroupName + "] 마감 임박!",
                    withOptionalStoreName("천원의 아침밥 수량이 얼마 남지 않았어요", storeName)
            );
            case OPEN -> new FcmMessage(
                    "[" + safeGroupName + "] 오픈 안내",
                    withOptionalStoreName("오늘도 천원의 아침밥이 오픈했어요!", storeName)
            );
        };
    }

    private static String withOptionalStoreName(String body, String storeName) {
        if (!APPEND_STORE_NAME_IN_BODY || storeName == null || storeName.isBlank()) {
            return body;
        }
        return body + " (" + storeName + ")";
    }

    public record FcmMessage(String title, String body) {
    }
}
