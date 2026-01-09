package com._1000meal.fcm.repository;

import com._1000meal.fcm.domain.FcmSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmSettingRepository extends JpaRepository<FcmSetting, Long> {
    Optional<FcmSetting> findByAccount_Id(Long accountId);
    boolean existsByAccount_Id(Long accountId);
}