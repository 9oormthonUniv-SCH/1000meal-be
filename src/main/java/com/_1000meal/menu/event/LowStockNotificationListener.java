package com._1000meal.menu.event;

import com._1000meal.fcm.service.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockNotificationListener {

    private final FcmPushService fcmPushService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock(LowStockEvent event) {
        log.info("[LOW_STOCK EVENT] storeId={}, groupId={}, groupName={}, remaining={}",
                event.storeId(), event.groupId(), event.groupName(), event.remainingStock());

        fcmPushService.sendLowStockNotification(
                event.storeId(),
                event.storeName(),
                event.groupId(),
                event.groupName(),
                event.remainingStock()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock30(LowStock30Event event) {
        log.info("[LOW_STOCK_30 EVENT] storeId={}, groupId={}, groupName={}, remaining={}",
                event.storeId(), event.groupId(), event.groupName(), event.remainingStock());

        fcmPushService.sendLowStock30Notification(
                event.storeId(),
                event.storeName(),
                event.groupId(),
                event.groupName(),
                event.remainingStock()
        );
    }
}
