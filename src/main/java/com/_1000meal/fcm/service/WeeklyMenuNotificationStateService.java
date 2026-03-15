package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.WeeklyMenuNotificationState;
import com._1000meal.fcm.domain.WeeklyMenuNotificationStatus;
import com._1000meal.fcm.repository.WeeklyMenuNotificationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklyMenuNotificationStateService {

    private final WeeklyMenuNotificationStateRepository repository;

    @Transactional(readOnly = true)
    public Optional<WeeklyMenuNotificationStatus> findStatus(Long storeId, Long menuGroupId, String weekKey) {
        return repository.findByStoreIdAndMenuGroupIdAndWeekKey(storeId, menuGroupId, weekKey)
                .map(WeeklyMenuNotificationState::getStatus);
    }

    @Transactional
    public void markPendingLate(Long storeId, Long menuGroupId, String weekKey) {
        upsertStatus(storeId, menuGroupId, weekKey, WeeklyMenuNotificationStatus.PENDING_LATE);
    }

    @Transactional
    public void markSent(Long storeId, Long menuGroupId, String weekKey) {
        upsertStatus(storeId, menuGroupId, weekKey, WeeklyMenuNotificationStatus.SENT);
    }

    @Transactional
    public void markClosedNotSent(Long storeId, Long menuGroupId, String weekKey) {
        upsertStatus(storeId, menuGroupId, weekKey, WeeklyMenuNotificationStatus.CLOSED_NOT_SENT);
    }

    private void upsertStatus(
            Long storeId,
            Long menuGroupId,
            String weekKey,
            WeeklyMenuNotificationStatus nextStatus
    ) {
        WeeklyMenuNotificationState state = repository.findByStoreIdAndMenuGroupIdAndWeekKey(
                        storeId, menuGroupId, weekKey
                )
                .orElseGet(() -> WeeklyMenuNotificationState.create(
                        storeId, menuGroupId, weekKey, nextStatus
                ));

        if (state.getId() == null) {
            repository.save(state);
            return;
        }

        // SENT나 CLOSED_NOT_SENT는 더 이상 뒤로 되돌리지 않습니다.
        if (nextStatus == WeeklyMenuNotificationStatus.PENDING_LATE
                && state.getStatus() != WeeklyMenuNotificationStatus.PENDING_LATE) {
            return;
        }

        if (state.getStatus() != nextStatus) {
            state.changeStatus(nextStatus);
        }
    }
}
