package com._1000meal.qr.repository;

import com._1000meal.qr.domain.MealUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealUsageRepository extends JpaRepository<MealUsage, Long> {
    Optional<MealUsage> findTop1ByUserIdAndUsedDateOrderByUsedAtDesc(Long userId, LocalDate usedDate);

    java.util.List<MealUsage> findAllByStoreIdAndUsedDateOrderByUsedAtAsc(Long storeId, LocalDate usedDate);

    java.util.List<MealUsage> findAllByUsedDateOrderByUsedAtAsc(LocalDate usedDate);

    @org.springframework.data.jpa.repository.Query("""
            SELECT mu FROM MealUsage mu
            WHERE mu.usedDate = :usedDate
            ORDER BY mu.store.id ASC,
                     CASE WHEN mu.menuGroupId IS NULL THEN 1 ELSE 0 END ASC,
                     mu.menuGroupId ASC,
                     mu.usedAt ASC
            """)
    java.util.List<MealUsage> findAllByUsedDateOrderByStoreAndGroupAndUsedAt(
            @org.springframework.data.repository.query.Param("usedDate") LocalDate usedDate
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from MealUsage mu where mu.usedDate < :cutoff")
    int deleteByUsedDateBefore(@org.springframework.data.repository.query.Param("cutoff") LocalDate cutoff);

    @Query("SELECT DISTINCT mu.user.id FROM MealUsage mu WHERE mu.usedDate = :usedDate")
    List<Long> findDistinctAccountIdsByUsedDate(@Param("usedDate") LocalDate usedDate);
}
