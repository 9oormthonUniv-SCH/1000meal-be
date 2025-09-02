package com._1000meal.auth.repository;


import com._1000meal.auth.model.AdminPasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminPasswordResetTokenRepository extends JpaRepository<AdminPasswordResetToken, Long> {
    Optional<AdminPasswordResetToken> findByTokenAndUsedFalse(String token);
    void deleteByAccountId(Long accountId); // confirm 후 정리용(선택)
}