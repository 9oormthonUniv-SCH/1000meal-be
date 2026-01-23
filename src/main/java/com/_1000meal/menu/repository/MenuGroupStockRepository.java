package com._1000meal.menu.repository;

import com._1000meal.menu.domain.MenuGroupStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MenuGroupStockRepository extends JpaRepository<MenuGroupStock, Long> {

    Optional<MenuGroupStock> findByMenuGroupId(Long menuGroupId);

    /**
     * 비관적 락을 사용한 재고 조회 (동시성 안전)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM MenuGroupStock s WHERE s.menuGroup.id = :groupId")
    Optional<MenuGroupStock> findByMenuGroupIdForUpdate(@Param("groupId") Long groupId);
}
