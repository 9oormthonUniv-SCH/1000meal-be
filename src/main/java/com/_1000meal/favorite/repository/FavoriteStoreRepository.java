package com._1000meal.favorite.repository;

import com._1000meal.favorite.domain.FavoriteStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {

    Optional<FavoriteStore> findByAccountIdAndStoreId(Long accountId, Long storeId);

    boolean existsByAccountIdAndStoreId(Long accountId, Long storeId);

    List<FavoriteStore> findAllByAccountId(Long accountId);

    long deleteByAccountIdAndStoreId(Long accountId, Long storeId);

    @Query("select fs.account.id from FavoriteStore fs where fs.store.id = :storeId")
    List<Long> findAccountIdsByStoreId(@Param("storeId") Long storeId);

    @Query("select fs from FavoriteStore fs join fetch fs.store where fs.account.id = :accountId")
    List<FavoriteStore> findAllByAccountIdWithStore(@Param("accountId") Long accountId);
}