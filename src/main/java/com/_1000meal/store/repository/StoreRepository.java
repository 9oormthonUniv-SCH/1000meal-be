package com._1000meal.store.repository;

import com._1000meal.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // 모든 Store의 id만 가져오는 쿼리 메서드
    @Query("SELECT s.id FROM Store s")
    List<Long> findAllStoreIds();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Store s SET s.isOpen = :status")
    int bulkUpdateStoreStatus(@Param("status") boolean status);
}
