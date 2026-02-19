package com._1000meal.qr.repository;

import com._1000meal.qr.domain.StoreQr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreQrRepository extends JpaRepository<StoreQr, Long> {
    Optional<StoreQr> findByQrTokenAndIsActiveTrue(String qrToken);
}
