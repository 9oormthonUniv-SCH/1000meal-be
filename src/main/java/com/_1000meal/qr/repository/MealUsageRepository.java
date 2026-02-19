package com._1000meal.qr.repository;

import com._1000meal.qr.domain.MealUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealUsageRepository extends JpaRepository<MealUsage, Long> {
}
