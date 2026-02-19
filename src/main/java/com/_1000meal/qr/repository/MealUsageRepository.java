package com._1000meal.qr.repository;

import com._1000meal.qr.domain.MealUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MealUsageRepository extends JpaRepository<MealUsage, Long> {
    Optional<MealUsage> findTop1ByUserIdAndUsedDate(Long userId, LocalDate usedDate);
}
