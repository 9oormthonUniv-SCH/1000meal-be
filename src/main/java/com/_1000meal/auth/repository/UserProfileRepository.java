package com._1000meal.auth.repository;

import com._1000meal.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // ✅ 프로필은 계정의 FK(account_id)로 접근하는 것이 자연스러움
    Optional<UserProfile> findByAccountId(Long accountId);
}