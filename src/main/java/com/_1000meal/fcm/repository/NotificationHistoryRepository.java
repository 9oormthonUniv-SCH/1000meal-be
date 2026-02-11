package com._1000meal.fcm.repository;

import com._1000meal.fcm.domain.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
}
