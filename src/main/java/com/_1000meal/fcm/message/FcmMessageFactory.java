package com._1000meal.fcm.message;

import com._1000meal.fcm.domain.NotificationType;

public final class FcmMessageFactory {

    private static final boolean APPEND_STORE_NAME_IN_BODY = false;

    private FcmMessageFactory() {
    }

    public static FcmMessage of(NotificationType type, String storeName, String groupName, int remain) {
        String safeGroupName = (groupName == null || groupName.isBlank()) ? storeName : groupName;

        return switch (type) {
            case OPEN_REMINDER -> new FcmMessage(
                    "천원의 아침밥 오픈 사전 알림",
                    "10분 후 천원의 아침밥이 시작돼요."
            );
            case LOW_STOCK_30 -> new FcmMessage(
                    "[" + safeGroupName + "] 마감 임박!",
                    withOptionalStoreName("천원의 아침밥 수량이 30개 이하로 줄었어요!", storeName)
            );
            // LOW_STOCK_10 알림 비활성화 (발행/리스너 주석 처리됨)
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
            case WEEKLY_MENU_UPLOADED -> new FcmMessage(
                    "이번 주 메뉴가 올라왔어요",
                    "[" + storeName + "]에 천원의 아침밥 메뉴를 확인해보세요"
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
