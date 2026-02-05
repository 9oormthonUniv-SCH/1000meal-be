package com._1000meal.menu.repository;

import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {


    @Query("SELECT wm FROM WeeklyMenu wm " +
            "LEFT JOIN FETCH wm.dailyMenus dm " +
            "LEFT JOIN FETCH dm.menus m " +
            "WHERE wm.store.id = :storeId " +
            "AND :today BETWEEN wm.startDate AND wm.endDate")
    Optional<WeeklyMenu> findByStoreIdAndRangeWithMenus(@Param("storeId") Long storeId, @Param("today") LocalDate today);

    @Query("SELECT wm FROM WeeklyMenu wm " +
            "LEFT JOIN FETCH wm.dailyMenus dm " +
            "WHERE wm.store.id = :storeId " +
            "AND :today BETWEEN wm.startDate AND wm.endDate")
    Optional<WeeklyMenu> findByStoreIdAndRangeWithDailyMenus(@Param("storeId") Long storeId, @Param("today") LocalDate today);
}