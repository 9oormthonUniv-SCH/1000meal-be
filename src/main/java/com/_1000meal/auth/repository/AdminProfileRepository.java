package com._1000meal.auth.repository;

import com._1000meal.auth.model.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {

    Optional<AdminProfile> findByAccountId(Long accountId);
}