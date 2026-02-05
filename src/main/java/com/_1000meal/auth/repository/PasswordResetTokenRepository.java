package com._1000meal.auth.repository;


import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.PasswordResetToken;
//import com._1000meal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;


//public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
//
//    // 재설정 링크 확인
//    Optional<PasswordResetToken> findByToken(String token);
//
//    // 레이트리밋: 특정 계정이 최근 N초(분) 내 생성한 토큰 개수
//    long countByAccountAndCreatedAtAfter(Account account, LocalDateTime after);
//
//    // (선택) 만료/사용된 토큰 정리용 유틸
//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query("delete from PasswordResetToken t where t.expiresAt < :now or t.usedAt is not null")
//    int deleteExpiredOrUsed(LocalDateTime now);
//}

//public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
//
//    // 평문이 아닌 해시로 조회
//    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
//
//    long countByAccountAndCreatedAtAfter(Account account, LocalDateTime after);
//
//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query("delete from PasswordResetToken t where t.expiresAt < :now or t.usedAt is not null")
//    int deleteExpiredOrUsed(LocalDateTime now);
//}


public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 레이트리밋(최근 쿨타임 내 생성된 토큰 수)
    long countByAccountAndCreatedAtAfter(Account account, LocalDateTime after);

    // 6자리 코드(해시) 기반 조회: 아직 사용되지 않았고 유효시간 내, 최신 1건
    Optional<PasswordResetToken> findTopByAccountAndTokenHashAndUsedAtIsNullAndExpiresAtAfterOrderByIdDesc(
            Account account, String tokenHash, LocalDateTime now
    );

    Optional<PasswordResetToken> findTopByAccountAndUsedAtIsNullOrderByIdDesc(Account account);
}