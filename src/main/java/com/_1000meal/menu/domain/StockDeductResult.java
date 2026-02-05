package com._1000meal.menu.domain;

/**
 * 재고 차감 결과
 * 각 임계치(30, 10)별 알림 발송 필요 여부를 반환한다.
 */
public record StockDeductResult(
        boolean notifyLowStock30,
        boolean notifyLowStock10
) {
    public boolean shouldNotify() {
        return notifyLowStock30 || notifyLowStock10;
    }
}
