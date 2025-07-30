package com._1000meal.user.repository;

import com._1000meal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // OAuth 로그인 시 이메일로 유저 확인
}