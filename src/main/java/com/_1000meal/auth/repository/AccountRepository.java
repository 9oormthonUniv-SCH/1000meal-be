package com._1000meal.auth.repository;

import com._1000meal.auth.model.Account;
import com._1000meal.global.constant.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsernameOrEmail(String username, String email);

    @Query("""
        select a from Account a
        where a.role = :role
          and (a.username = :identifier or a.email = :identifier)
    """)
    Optional<Account> findByRoleAndIdentifier(Role role, String identifier);
}