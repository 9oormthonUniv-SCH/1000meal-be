package com._1000meal.fcm.repository;

import com._1000meal.fcm.domain.WeeklyMenuNotificationState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyMenuNotificationStateRepository extends JpaRepository<WeeklyMenuNotificationState, Long> {

    Optional<WeeklyMenuNotificationState> findByStoreIdAndMenuGroupIdAndWeekKey(
            Long storeId,
            Long menuGroupId,
            String weekKey
    );
}
