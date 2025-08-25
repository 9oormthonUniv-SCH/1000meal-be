package com._1000meal.auth.repository;

import com._1000meal.auth.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // ✅ 유니크 체크는 분리: 이메일이 null일 수 있는 케이스 방어에 유리
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ✅ 로그인: userId 또는 email 어느 쪽으로든 단일 메서드로 조회
    Optional<Account> findByUsernameOrEmail(String username, String email);
}