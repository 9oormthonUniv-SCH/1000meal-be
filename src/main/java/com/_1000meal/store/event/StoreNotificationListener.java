package com._1000meal.store.event;

import com._1000meal.fcm.service.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreNotificationListener {

    private final FcmPushService fcmPushService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStoreOpened(StoreOpenedEvent event) {
        log.info("[OPEN EVENT] storeId={}, storeName={}", event.storeId(), event.storeName());
        fcmPushService.sendStoreOpened(event.storeId(), event.storeName());
    }
}