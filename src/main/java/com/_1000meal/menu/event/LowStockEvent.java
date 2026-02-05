package com._1000meal.menu.event;

/**
 * 품절 임박 알림 이벤트
 * 그룹 재고가 10 이하로 떨어질 때 발행됨
 */
public record LowStockEvent(
        Long storeId,
        String storeName,
        Long groupId,
        String groupName,
        int remainingStock
) {
}
