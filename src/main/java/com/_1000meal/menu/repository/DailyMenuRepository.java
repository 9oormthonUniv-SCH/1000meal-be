package com._1000meal.menu.repository;

import com._1000meal.menu.domain.DailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {

    @Query("SELECT dm FROM DailyMenu dm " +
            "LEFT JOIN FETCH dm.menus " +
            "WHERE dm.weeklyMenu.store.id = :storeId AND dm.date = :date")
    Optional<DailyMenu> findDailyMenuByStoreIdAndDate(@Param("storeId") Long storeId, @Param("date") LocalDate date);
}
