package com._1000meal.qr.repository;

import com._1000meal.qr.domain.MealUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MealUsageRepository extends JpaRepository<MealUsage, Long> {
    Optional<MealUsage> findTop1ByUserIdAndUsedDate(Long userId, LocalDate usedDate);

    java.util.List<MealUsage> findAllByStoreIdAndUsedDateOrderByUsedAtAsc(Long storeId, LocalDate usedDate);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from MealUsage mu where mu.usedDate < :cutoff")
    int deleteByUsedDateBefore(@org.springframework.data.repository.query.Param("cutoff") LocalDate cutoff);
}
