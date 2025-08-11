package com._1000meal.user.repository;

import com._1000meal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    Optional<User> findByUserId(String userId);
}
