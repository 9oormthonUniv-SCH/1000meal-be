package com._1000meal.menu.repository;

import com._1000meal.menu.domain.DailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {

    @Query("SELECT dm FROM DailyMenu dm " +
            "LEFT JOIN FETCH dm.menus " +
            "WHERE dm.weeklyMenu.store.id = :storeId AND dm.date = :date")
    Optional<DailyMenu> findDailyMenuByStoreIdAndDate(@Param("storeId") Long storeId, @Param("date") LocalDate date);

    @Query("select dm.date from DailyMenu dm where dm.weeklyMenu.id = :weeklyId")
    List<LocalDate> findDatesByWeeklyMenuId(@Param("weeklyId") Long weeklyId);

    @Query("""
        select dm.stock
        from DailyMenu dm
        join dm.weeklyMenu wm
        join wm.store s
        where s.id = :storeId
          and dm.date = :date
    """)
    Optional<Integer> findStockByStoreIdAndDate(@Param("storeId") Long storeId,
                                                @Param("date") LocalDate date);

    @Query("""
        SELECT COALESCE(SUM(mgs.stock), 0)
        FROM MenuGroupStock mgs
        JOIN mgs.menuGroup mg
        JOIN mg.dailyMenu dm
        WHERE dm.weeklyMenu.store.id = :storeId AND dm.date = :date
    """)
    Optional<Integer> findTotalGroupStockByStoreIdAndDate(@Param("storeId") Long storeId,
                                                          @Param("date") LocalDate date);
}
