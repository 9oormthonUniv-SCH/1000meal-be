package com._1000meal.fcm.service;

import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.dto.StockDeadlineCandidate;
import com._1000meal.qr.repository.MealUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeadlineNotificationService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final MealUsageRepository mealUsageRepository;
    private final StockDeadlineNotificationPolicy policy;
    private final NotificationHistoryService historyService;
    private final FcmPushService fcmPushService;

    public void sendStockDeadlineNotifications(LocalDate date) {
        List<StockDeadlineCandidate> candidates = favoriteStoreRepository.findStockDeadlineCandidates();
        if (candidates.isEmpty()) {
            log.info("[FCM][STOCK_DEADLINE] no favorite targets");
            return;
        }

        Set<Long> accountIdsUsedQrToday = new HashSet<>(mealUsageRepository.findDistinctAccountIdsByUsedDate(date));

        int sentCount = 0;
        int dedupSkipCount = 0;
        int policySkipCount = 0;
        int qrUsedTodaySkipCount = 0;

        Map<String, List<StockDeadlineCandidate>> grouped = new HashMap<>();
        for (StockDeadlineCandidate c : candidates) {
            String key = c.accountId() + ":" + c.storeId();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        for (List<StockDeadlineCandidate> group : grouped.values()) {
            List<StockDeadlineCandidate> targets = selectTargets(group);
            for (StockDeadlineCandidate target : targets) {
                if (accountIdsUsedQrToday.contains(target.accountId())) {
                    qrUsedTodaySkipCount++;
                    continue;
                }
                int remain = target.groupStock() != null ? target.groupStock() : safeRemain(target.storeRemain());
                if (!policy.canSend(remain)) {
                    policySkipCount++;
                    continue;
                }

                boolean recorded = historyService.tryMarkSent(
                        NotificationType.STOCK_DEADLINE,
                        target.accountId(),
                        target.storeId(),
                        target.menuGroupId(),
                        date,
                        null
                );
                if (!recorded) {
                    dedupSkipCount++;
                    continue;
                }

                fcmPushService.sendStockDeadlineNotification(
                        target.accountId(),
                        target.storeId(),
                        target.storeName(),
                        target.storeImageUrl(),
                        target.menuGroupId(),
                        target.menuGroupName(),
                        remain
                );
                sentCount++;
            }
        }
        log.info("[FCM][STOCK_DEADLINE] run summary. date={}, candidates={}, groups={}, sent={}, dedupSkip={}, policySkip={}, qrUsedTodaySkip={}",
                date, candidates.size(), grouped.size(), sentCount, dedupSkipCount, policySkipCount, qrUsedTodaySkipCount);
    }

    private List<StockDeadlineCandidate> selectTargets(List<StockDeadlineCandidate> candidates) {
        boolean hasGroupStock = candidates.stream().anyMatch(c -> c.groupStock() != null);
        if (hasGroupStock) {
            List<StockDeadlineCandidate> withStock = new ArrayList<>();
            for (StockDeadlineCandidate c : candidates) {
                if (c.groupStock() != null) {
                    withStock.add(c);
                }
            }
            return withStock;
        }

        StockDeadlineCandidate chosen = candidates.get(0);
        for (StockDeadlineCandidate c : candidates) {
            if (c.menuGroupSortOrder() != null && chosen.menuGroupSortOrder() != null) {
                if (c.menuGroupSortOrder() < chosen.menuGroupSortOrder()) {
                    chosen = c;
                }
            }
        }
        return List.of(chosen);
    }

    private int safeRemain(Integer remain) {
        return remain != null ? remain : 0;
    }
}
