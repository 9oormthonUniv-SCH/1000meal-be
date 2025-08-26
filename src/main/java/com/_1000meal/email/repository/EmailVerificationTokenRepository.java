package com._1000meal.email.repository;

import com._1000meal.email.domain.EmailVerificationToken;
import com._1000meal.email.domain.TokenStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findTop1ByEmailAndVerifiedFalseOrderByIdDesc(String email);
    Optional<EmailVerificationToken> findTop1ByEmailAndVerifiedTrueOrderByIdDesc(String email);
    boolean existsByEmailAndVerifiedTrue(String email);
    void deleteByEmailAndVerifiedFalse(String email);
    void deleteByEmail(String email);
}