package com._1000meal.fcm.service;

import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.dto.OpenNotificationTarget;
import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.store.dto.StoreTodayMenuDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenNotificationService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final MenuGroupService menuGroupService;
    private final NotificationHistoryService historyService;
    private final FcmPushService fcmPushService;
    private final OpenNotificationStorePolicy storePolicy;

    public void sendDailyOpenNotifications(LocalDate date) {
        if (isWeekend(date)) {
            log.info("[FCM][OPEN] weekend skip. date={}", date);
            return;
        }

        List<OpenNotificationTarget> targets = favoriteStoreRepository.findOpenNotificationTargets();
        if (targets.isEmpty()) {
            log.info("[FCM][OPEN] no favorite targets");
            return;
        }

        Set<Long> storeIds = new HashSet<>();
        for (OpenNotificationTarget target : targets) {
            storeIds.add(target.storeId());
        }

        Map<Long, StoreTodayMenuDto> todayMenus =
                menuGroupService.getTodayMenuForStores(List.copyOf(storeIds), date);

        for (OpenNotificationTarget target : targets) {
            boolean hasTodayMenu = hasTodayMenu(todayMenus.get(target.storeId()));
            if (!storePolicy.canSend(target, hasTodayMenu, date)) {
                logSkipReason(target, hasTodayMenu, date);
                continue;
            }

            boolean recorded = historyService.tryMarkSent(
                    NotificationType.OPEN,
                    target.accountId(),
                    target.storeId(),
                    null,
                    date
            );
            if (!recorded) {
                log.info("[FCM][OPEN] duplicate skip. accountId={}, storeId={}",
                        target.accountId(), target.storeId());
                continue;
            }

            fcmPushService.sendOpenNotification(
                    target.accountId(),
                    target.storeId(),
                    target.storeName(),
                    target.storeImageUrl()
            );
        }
    }

    private boolean hasTodayMenu(StoreTodayMenuDto dto) {
        if (dto == null || dto.getMenuGroups() == null || dto.getMenuGroups().isEmpty()) {
            return false;
        }
        return dto.getMenuGroups().stream()
                .anyMatch(group -> group.getMenus() != null && !group.getMenus().isEmpty());
    }

    private void logSkipReason(OpenNotificationTarget target, boolean hasTodayMenu, LocalDate date) {
        if (isWeekend(date)) {
            log.info("[FCM][OPEN] weekend skip. accountId={}, storeId={}",
                    target.accountId(), target.storeId());
            return;
        }
        if (!target.storeIsOpen()) {
            log.info("[FCM][OPEN] closed skip. accountId={}, storeId={}",
                    target.accountId(), target.storeId());
            return;
        }
        if (!hasTodayMenu) {
            log.info("[FCM][OPEN] no_menu skip. accountId={}, storeId={}",
                    target.accountId(), target.storeId());
        }
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
