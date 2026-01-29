package com._1000meal.menu.repository;


import com._1000meal.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Modifying // ← 자동 flush/clear 빼는 게 안전
    @Query("delete from Menu m where m.dailyMenu.id = :dailyMenuId")
    int deleteByDailyMenuId(@Param("dailyMenuId") Long dailyMenuId);

    List<Menu> findByDailyMenu_IdOrderByIdAsc(Long dailyMenuId);

    @Query("select m from Menu m where m.dailyMenu.id in :dailyMenuIds order by m.id asc")
    List<Menu> findByDailyMenuIdInOrderByIdAsc(@Param("dailyMenuIds") List<Long> dailyMenuIds);
}
