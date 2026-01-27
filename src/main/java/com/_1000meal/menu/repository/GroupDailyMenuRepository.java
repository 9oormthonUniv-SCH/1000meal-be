package com._1000meal.menu.repository;

import com._1000meal.menu.domain.GroupDailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface GroupDailyMenuRepository extends JpaRepository<GroupDailyMenu, Long> {

    Optional<GroupDailyMenu> findByMenuGroupIdAndDate(Long menuGroupId, LocalDate date);

    @Query("SELECT DISTINCT gdm FROM GroupDailyMenu gdm " +
            "LEFT JOIN FETCH gdm.menuNames " +
            "WHERE gdm.menuGroup.id IN :groupIds AND gdm.date = :date")
    List<GroupDailyMenu> findByMenuGroupIdInAndDate(
            @Param("groupIds") List<Long> groupIds,
            @Param("date") LocalDate date
    );

    @Query("SELECT DISTINCT gdm FROM GroupDailyMenu gdm " +
            "LEFT JOIN FETCH gdm.menuNames " +
            "WHERE gdm.menuGroup.id IN :groupIds AND gdm.date BETWEEN :startDate AND :endDate")
    List<GroupDailyMenu> findByMenuGroupIdInAndDateBetween(
            @Param("groupIds") List<Long> groupIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
