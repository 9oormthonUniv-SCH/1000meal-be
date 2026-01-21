package com._1000meal.fcm.repository;

import com._1000meal.fcm.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByAccountId(Long accountId);

    // 알림 ON인 계정 id만 뽑기
    @Query("select p.accountId from NotificationPreference p where p.enabled = true")
    List<Long> findEnabledAccountIds();
}