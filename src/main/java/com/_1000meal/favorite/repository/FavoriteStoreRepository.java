package com._1000meal.favorite.repository;

import com._1000meal.favorite.domain.FavoriteStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {

    Optional<FavoriteStore> findByAccountIdAndStoreId(Long accountId, Long storeId);

    boolean existsByAccountIdAndStoreId(Long accountId, Long storeId);

    List<FavoriteStore> findAllByAccountId(Long accountId);

    long deleteByAccountIdAndStoreId(Long accountId, Long storeId);
}