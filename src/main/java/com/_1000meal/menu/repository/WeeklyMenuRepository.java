package com._1000meal.menu.repository;

import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

// WeeklyMenuRepository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {

    @Query("""
        select distinct wm
        from WeeklyMenu wm
        join wm.store s
        left join fetch wm.dailyMenus dm
        left join fetch dm.menus m
        where s.id = :storeId
          and wm.startDate <= :endDate
          and wm.endDate   >= :startDate
    """)
    Optional<WeeklyMenu> findByStoreIdAndRangeWithMenus(@Param("storeId") Long storeId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}