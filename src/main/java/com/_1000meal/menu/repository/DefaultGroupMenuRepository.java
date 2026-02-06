package com._1000meal.menu.repository;

import com._1000meal.menu.domain.DefaultGroupMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DefaultGroupMenuRepository extends JpaRepository<DefaultGroupMenu, Long> {

    @Query("SELECT dgm FROM DefaultGroupMenu dgm " +
            "WHERE dgm.menuGroup.id = :groupId " +
            "AND dgm.menuName = :menuName " +
            "AND dgm.endDate IS NULL")
    Optional<DefaultGroupMenu> findOpenRule(@Param("groupId") Long groupId, @Param("menuName") String menuName);

    @Query("SELECT dgm FROM DefaultGroupMenu dgm " +
            "WHERE dgm.menuGroup.id IN :groupIds " +
            "AND dgm.active = true " +
            "AND dgm.startDate <= :date " +
            "AND (dgm.endDate IS NULL OR dgm.endDate >= :date) " +
            "ORDER BY dgm.menuGroup.id ASC, dgm.startDate ASC, dgm.id ASC")
    List<DefaultGroupMenu> findApplicableByMenuGroupIdsAndDate(
            @Param("groupIds") List<Long> groupIds,
            @Param("date") LocalDate date
    );

    @Query("SELECT dgm FROM DefaultGroupMenu dgm " +
            "WHERE dgm.menuGroup.id = :groupId " +
            "AND dgm.active = true " +
            "AND dgm.startDate <= :date " +
            "AND (dgm.endDate IS NULL OR dgm.endDate >= :date) " +
            "ORDER BY dgm.startDate ASC, dgm.id ASC")
    List<DefaultGroupMenu> findActiveByMenuGroupIdAndDate(
            @Param("groupId") Long groupId,
            @Param("date") LocalDate date
    );

    @Query("SELECT dgm FROM DefaultGroupMenu dgm " +
            "WHERE dgm.menuGroup.id = :groupId " +
            "ORDER BY dgm.startDate DESC, dgm.id DESC")
    List<DefaultGroupMenu> findByMenuGroupIdOrderByStartDateDescIdDesc(@Param("groupId") Long groupId);
}
