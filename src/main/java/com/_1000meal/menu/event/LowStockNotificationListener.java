package com._1000meal.menu.event;

import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.service.FcmPushService;
import com._1000meal.fcm.service.NotificationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockNotificationListener {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final FcmPushService fcmPushService;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final NotificationHistoryService historyService;

    // LOW_STOCK_10 알림 비활성화
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock(LowStockEvent event) {
        // log.info("[LOW_STOCK EVENT] storeId={}, groupId={}, groupName={}, remaining={}",
        //         event.storeId(), event.groupId(), event.groupName(), event.remainingStock());
        // LocalDate today = LocalDate.now(ZONE_ID);
        // List<Long> accountIds = favoriteStoreRepository.findFavoriteSubscriberAccountIdsByStoreId(event.storeId());
        // for (Long accountId : accountIds) {
        //     boolean recorded = historyService.tryMarkSent(
        //             NotificationType.LOW_STOCK_10,
        //             accountId,
        //             event.storeId(),
        //             event.groupId(),
        //             today,
        //             null
        //     );
        //     if (!recorded) {
        //         log.info("[FCM][LOW_STOCK] duplicate skip. accountId={}, storeId={}, groupId={}",
        //                 accountId, event.storeId(), event.groupId());
        //         continue;
        //     }
        //     fcmPushService.sendLowStockNotificationForAccount(
        //             accountId,
        //             event.storeId(),
        //             event.storeName(),
        //             event.groupId(),
        //             event.groupName(),
        //             event.remainingStock()
        //     );
        // }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock30(LowStock30Event event) {
        log.info("[LOW_STOCK_30 EVENT] storeId={}, groupId={}, groupName={}, remaining={}",
                event.storeId(), event.groupId(), event.groupName(), event.remainingStock());

        LocalDate today = LocalDate.now(ZONE_ID);
        List<Long> accountIds = favoriteStoreRepository.findFavoriteSubscriberAccountIdsByStoreId(event.storeId());
        for (Long accountId : accountIds) {
            boolean recorded = historyService.tryMarkSent(
                    NotificationType.LOW_STOCK_30,
                    accountId,
                    event.storeId(),
                    event.groupId(),
                    today,
                    null
            );
            if (!recorded) {
                log.info("[FCM][LOW_STOCK_30] duplicate skip. accountId={}, storeId={}, groupId={}",
                        accountId, event.storeId(), event.groupId());
                continue;
            }

            fcmPushService.sendLowStock30NotificationForAccount(
                    accountId,
                    event.storeId(),
                    event.storeName(),
                    event.groupId(),
                    event.groupName(),
                    event.remainingStock()
            );
        }
    }
}
