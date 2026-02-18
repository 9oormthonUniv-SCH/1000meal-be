package com._1000meal.favorite.repository;

import com._1000meal.favorite.domain.FavoriteStore;
import com._1000meal.favorite.dto.FavoriteStoreResponse;
import com._1000meal.fcm.dto.OpenNotificationTarget;
import com._1000meal.fcm.dto.StockDeadlineCandidate;
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

    @Query("""
        select new com._1000meal.favorite.dto.FavoriteStoreResponse(
            fs.store.id,
            fs.store.name,
            fs.store.imageUrl,
            fs.store.isOpen
        )
        from FavoriteStore fs
        join fs.store
        where fs.account.id = :accountId
        order by fs.id desc
    """)
    List<FavoriteStoreResponse> findFavoriteStores(@Param("accountId") Long accountId);

    @Query("""
        select new com._1000meal.fcm.dto.OpenNotificationTarget(
            fs.account.id,
            fs.store.id,
            fs.store.name,
            fs.store.imageUrl,
            fs.store.isOpen
        )
        from FavoriteStore fs
        join NotificationPreference np on np.accountId = fs.account.id
        where np.enabled = true
    """)
    List<OpenNotificationTarget> findOpenNotificationTargets();

    @Query("""
        select new com._1000meal.fcm.dto.StockDeadlineCandidate(
            fs.account.id,
            s.id,
            s.name,
            s.imageUrl,
            mg.id,
            mg.name,
            mg.sortOrder,
            mgs.stock,
            s.remain
        )
        from FavoriteStore fs
        join fs.store s
        join MenuGroup mg on mg.store.id = s.id
        left join mg.stock mgs
        join NotificationPreference np on np.accountId = fs.account.id
        where np.enabled = true
        order by fs.account.id asc, s.id asc, mg.sortOrder asc, mg.id asc
    """)
    List<StockDeadlineCandidate> findStockDeadlineCandidates();

    @Query("""
        select fs.account.id
        from FavoriteStore fs
        join NotificationPreference np on np.accountId = fs.account.id
        where fs.store.id = :storeId
          and np.enabled = true
    """)
    List<Long> findFavoriteSubscriberAccountIdsByStoreId(@Param("storeId") Long storeId);
}
