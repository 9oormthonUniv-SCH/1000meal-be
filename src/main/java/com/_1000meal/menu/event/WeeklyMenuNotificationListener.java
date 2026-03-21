package com._1000meal.menu.event;

import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.service.FcmPushService;
import com._1000meal.fcm.service.NotificationHistoryService;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyMenuNotificationListener {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final NotificationHistoryService historyService;
    private final FcmPushService fcmPushService;
    private final StoreRepository storeRepository;
    private final MenuGroupRepository menuGroupRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWeeklyMenuUploaded(WeeklyMenuUploadedEvent event) {
        Store store = storeRepository.findById(event.storeId()).orElse(null);
        if (store == null) {
            log.info("[WEEKLY_MENU] store not found. storeId={}", event.storeId());
            return;
        }

        List<Long> accountIds =
                favoriteStoreRepository.findFavoriteSubscriberAccountIdsByStoreId(event.storeId());
        if (accountIds.isEmpty()) {
            log.info("[WEEKLY_MENU] no subscribers. storeId={}", event.storeId());
            return;
        }

        for (Long groupId : event.menuGroupIds()) {
            MenuGroup group = menuGroupRepository.findById(groupId).orElse(null);
            if (group == null) {
                log.info("[WEEKLY_MENU] group not found. groupId={}", groupId);
                continue;
            }
            String groupName = group.getName();

            for (Long accountId : accountIds) {
                boolean recorded = historyService.tryMarkSent(
                        NotificationType.WEEKLY_MENU_UPLOADED,
                        accountId,
                        store.getId(),
                        groupId,
                        event.weekStart(),
                        event.weekKey()
                );
                if (!recorded) {
                    continue;
                }

                fcmPushService.sendWeeklyMenuUploadedNotification(
                        accountId,
                        store.getId(),
                        store.getName(),
                        groupId,
                        groupName,
                        store.getImageUrl(),
                        event.weekKey()
                );
            }
        }
    }

    // 이미 메뉴 업로드 알림을 보낸 뒤 메뉴가 수정된 경우, "메뉴 변경" 알림을 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWeeklyMenuChanged(WeeklyMenuChangedEvent event) {
        Store store = storeRepository.findById(event.storeId()).orElse(null);
        if (store == null) {
            log.info("[WEEKLY_MENU_CHANGED] store not found. storeId={}", event.storeId());
            return;
        }

        List<Long> accountIds =
                favoriteStoreRepository.findFavoriteSubscriberAccountIdsByStoreId(event.storeId());
        if (accountIds.isEmpty()) {
            log.info("[WEEKLY_MENU_CHANGED] no subscribers. storeId={}", event.storeId());
            return;
        }

        for (Long groupId : event.menuGroupIds()) {
            MenuGroup group = menuGroupRepository.findById(groupId).orElse(null);
            if (group == null) {
                log.info("[WEEKLY_MENU_CHANGED] group not found. groupId={}", groupId);
                continue;
            }
            String groupName = group.getName();

            for (Long accountId : accountIds) {
                fcmPushService.sendWeeklyMenuChangedNotification(
                        accountId,
                        store.getId(),
                        store.getName(),
                        groupId,
                        groupName,
                        store.getImageUrl(),
                        event.weekKey()
                );
            }
        }
    }
}
