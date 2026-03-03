package com._1000meal.qr.repository;

import com._1000meal.qr.domain.StoreQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreQrRepository extends JpaRepository<StoreQr, Long> {
    Optional<StoreQr> findByQrTokenAndIsActiveTrue(String qrToken);

    @Query("SELECT sq FROM StoreQr sq JOIN FETCH sq.store WHERE sq.qrToken = :qrToken AND sq.isActive = true")
    Optional<StoreQr> findActiveByQrTokenWithStore(@Param("qrToken") String qrToken);

    @Query("SELECT sq FROM StoreQr sq JOIN FETCH sq.store ORDER BY sq.id ASC")
    List<StoreQr> findAllWithStore();
}
