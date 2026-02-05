package com._1000meal.fcm.repository;

import com._1000meal.fcm.domain.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);
    List<FcmToken> findAllByAccountIdAndActiveTrue(Long accountId);

    // ON 계정들 토큰을 한번에
    List<FcmToken> findAllByAccountIdInAndActiveTrue(List<Long> accountIds);

    /**
     * 특정 매장을 즐겨찾기한 사용자 중 알림 ON이고 active 토큰을 보유한 토큰 목록 조회
     * - favorite_store.store_id = :storeId
     * - notification_preferences.enabled = true
     * - fcm_tokens.active = true
     */
    @Query("select ft from FcmToken ft " +
           "where ft.active = true " +
           "and ft.accountId in (" +
           "  select fs.account.id from FavoriteStore fs " +
           "  join NotificationPreference np on np.accountId = fs.account.id " +
           "  where fs.store.id = :storeId and np.enabled = true" +
           ")")
    List<FcmToken> findActiveTokensForFavoriteStore(@Param("storeId") Long storeId);
}