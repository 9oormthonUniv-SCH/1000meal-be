package com._1000meal.auth.repository;

import com._1000meal.auth.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    Optional<Account> findByUserId(String userId);
    Optional<Account> findByUserIdOrEmail(String userId, String email);

    Optional<Account> findByEmail(String email);
}