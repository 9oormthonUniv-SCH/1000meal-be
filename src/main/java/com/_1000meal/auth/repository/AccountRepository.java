package com._1000meal.auth.repository;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /* ====================== 가입/중복 체크 ====================== */
    /** 탈퇴(DELETED) 제외하고 userId 중복 여부 확인 */
    boolean existsByUserIdAndStatusNot(String userId, AccountStatus status);

    /** 탈퇴(DELETED) 제외하고 email 중복 여부 확인 */
    boolean existsByEmailAndStatusNot(String email, AccountStatus status);


    /* ====================== 단건 조회 (상태 고려) ====================== */
    /** 특정 상태(예: ACTIVE)의 이메일로 조회 */
    Optional<Account> findByEmailAndStatus(String email, AccountStatus status);

    /** 특정 상태(예: ACTIVE)의 학번으로 조회 */
    Optional<Account> findByUserIdAndStatus(String userId, AccountStatus status);

    /** 삭제 제외하고 id로 조회 */
    Optional<Account> findByIdAndStatusNot(Long id, AccountStatus status);

    /**
     * 로그인/비번변경 등에서: (userId = ? OR email = ?) AND status <> ?
     * 예) findByUserIdOrEmailAndStatusNot(key, key, AccountStatus.DELETED)
     */
    Optional<Account> findByUserIdOrEmailAndStatusNot(String userId, String email, AccountStatus status);


    /* ====================== (레거시) 가급적 사용 지양 ====================== */
    @Deprecated // 상태를 고려하지 않으므로 가능하면 위 메서드들 사용
    boolean existsByUserId(String userId);

    @Deprecated
    boolean existsByEmail(String email);

    @Deprecated
    Optional<Account> findByUserId(String userId);

    @Deprecated
    Optional<Account> findByUserIdOrEmail(String userId, String email);

    @Deprecated
    Optional<Account> findByEmail(String email);
}