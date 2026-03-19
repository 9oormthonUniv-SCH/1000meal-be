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

    // 주간 메뉴 알람 상태 조회
    @Transactional(readOnly = true)
    public Optional<WeeklyMenuNotificationStatus> findStatus(Long storeId, Long menuGroupId, String weekKey) {
        return repository.findByStoreIdAndMenuGroupIdAndWeekKey(storeId, menuGroupId, weekKey)
                .map(WeeklyMenuNotificationState::getStatus);
    }

    // 상태 표시 메서드
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

        /*
            이미 "발송 완료" 된 건 다시 "지연 대기중"으로 바꾸지 않는다
            이미 마감되어 "미발송" 처리된 것도 다시 "지연 대기중"으로 바꾸지 않는다
         */
        if (nextStatus == WeeklyMenuNotificationStatus.PENDING_LATE
                && state.getStatus() != WeeklyMenuNotificationStatus.PENDING_LATE) {
            return;
        }

        if (state.getStatus() != nextStatus) {
            state.changeStatus(nextStatus);
        }
    }
}
