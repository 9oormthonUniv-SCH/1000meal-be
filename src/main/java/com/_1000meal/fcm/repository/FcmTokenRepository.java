package com._1000meal.fcm.repository;

import com._1000meal.fcm.domain.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);
    List<FcmToken> findAllByAccountIdAndActiveTrue(Long accountId);

    // ON 계정들 토큰을 한번에
    List<FcmToken> findAllByAccountIdInAndActiveTrue(List<Long> accountIds);
}