package com._1000meal.email.repository;

import com._1000meal.email.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    // 최신 유효 토큰 조회(미검증, 미만료)
    Optional<EmailVerificationToken> findTop1ByEmailAndVerifiedFalseOrderByIdDesc(String email);

    boolean existsByEmailAndVerifiedTrue(String email);
}