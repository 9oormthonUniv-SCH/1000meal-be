package com._1000meal.auth.repository;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // ✅ 회원가입 중복 체크: DELETED 는 중복으로 보지 않음
    boolean existsByUserIdAndStatusNot(String userId, AccountStatus status);
    boolean existsByEmailAndStatusNot(String email, AccountStatus status);

    // ✅ 비밀번호 찾기/아이디 찾기 등: ACTIVE 계정만 대상으로
    Optional<Account> findByEmailAndStatus(String email, AccountStatus status);
    Optional<Account> findByUserIdAndStatus(String userId, AccountStatus status);

    // ✅ 로그인/비번변경 등: userId 또는 email 키로 ACTIVE/미삭제만 조회
    @Query("""
           select a
             from Account a
            where (a.userId = :key or a.email = :key)
              and a.status <> com._1000meal.auth.model.AccountStatus.DELETED
           """)
    Optional<Account> findActiveByUserIdOrEmail(@Param("key") String key);

    // ✅ (선택) id로 조회할 때도 삭제 제외
    Optional<Account> findByIdAndStatusNot(Long id, AccountStatus status);

    // ===== 기존 메서드(필요 시 유지) =====
    // 단, 아래 기본 exists/find는 가급적 새 메서드로 대체하는 걸 권장
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    Optional<Account> findByUserId(String userId);
    Optional<Account> findByUserIdOrEmail(String userId, String email);
    Optional<Account> findByEmail(String email);
}