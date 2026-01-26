package com._1000meal.menu.event;

/**
 * 재고 30 이하 알림 이벤트
 * 그룹 재고가 31 초과에서 30 이하로 떨어질 때 발행됨
 */
public record LowStock30Event(
        Long storeId,
        String storeName,
        Long groupId,
        String groupName,
        int remainingStock
) {
}
