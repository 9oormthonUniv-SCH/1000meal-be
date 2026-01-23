package com._1000meal.menu.repository;

import com._1000meal.menu.domain.MenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuGroupRepository extends JpaRepository<MenuGroup, Long> {

    List<MenuGroup> findByDailyMenuIdOrderBySortOrderAsc(Long dailyMenuId);

    @Query("SELECT mg FROM MenuGroup mg " +
            "LEFT JOIN FETCH mg.stock " +
            "LEFT JOIN FETCH mg.menus " +
            "WHERE mg.dailyMenu.id = :dailyMenuId " +
            "ORDER BY mg.sortOrder ASC")
    List<MenuGroup> findByDailyMenuIdWithStockAndMenus(@Param("dailyMenuId") Long dailyMenuId);

    @Query("SELECT mg FROM MenuGroup mg " +
            "JOIN FETCH mg.dailyMenu dm " +
            "JOIN FETCH dm.weeklyMenu wm " +
            "JOIN FETCH wm.store " +
            "LEFT JOIN FETCH mg.stock " +
            "WHERE mg.id = :groupId")
    Optional<MenuGroup> findByIdWithStore(@Param("groupId") Long groupId);

    @Query("SELECT mg FROM MenuGroup mg " +
            "LEFT JOIN FETCH mg.stock " +
            "WHERE mg.id = :groupId")
    Optional<MenuGroup> findByIdWithStock(@Param("groupId") Long groupId);
}
