package com._1000meal.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

//// com._1000meal.auth.model.PasswordResetToken
//@Entity
//@Getter
//@Table(name = "password_reset_token",
//        indexes = {
//                @Index(name = "idx_prt_token_hash", columnList = "token_hash", unique = true),
//                @Index(name = "idx_prt_account", columnList = "account_id")
//        })
//public class PasswordResetToken {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(optional = false, fetch = FetchType.LAZY)
//    @JoinColumn(name = "account_id", nullable = false)
//    private Account account;
//
//    // ✅ DB에는 해시만 저장
//    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
//    private String tokenHash; // SHA-256 hex(64자)
//
//    @Column(nullable = false)
//    private LocalDateTime expiresAt;
//
//    private LocalDateTime usedAt;
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt;
//
//    // ✅ 메일 발송용 평문 토큰 (DB 미저장)
//    @Transient
//    private String rawToken;
//
//    protected PasswordResetToken() {}
//
//    private PasswordResetToken(Account account, String rawToken, String tokenHash, LocalDateTime expiresAt) {
//        this.account = account;
//        this.rawToken = rawToken;
//        this.tokenHash = tokenHash;
//        this.expiresAt = expiresAt;
//        this.createdAt = LocalDateTime.now();
//    }
//
//    public static PasswordResetToken issue(Account account, long minutes) {
//        String raw = UUID.randomUUID().toString().replace("-", "");
//        String hash = sha256Hex(raw);
//        return new PasswordResetToken(account, raw, hash, LocalDateTime.now().plusMinutes(minutes));
//    }
//
//    public boolean isUsable() {
//        return usedAt == null && LocalDateTime.now().isBefore(expiresAt);
//    }
//
//    public void markUsed() { this.usedAt = LocalDateTime.now(); }
//
//    @PrePersist
//    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
//
//    // -- 해시 유틸 (간단 구현) --
//    private static String sha256Hex(String s) {
//        try {
//            var md = java.security.MessageDigest.getInstance("SHA-256");
//            byte[] digest = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
//            StringBuilder sb = new StringBuilder(digest.length * 2);
//            for (byte b : digest) sb.append(String.format("%02x", b));
//            return sb.toString();
//        } catch (Exception e) {
//            throw new IllegalStateException("SHA-256 not available", e);
//        }
//    }
//}

@Entity
@Getter
@NoArgsConstructor
@Table(name = "password_reset_token",
        indexes = {
                @Index(name = "idx_prt_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_prt_account", columnList = "account_id")
        })
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;  // DB에는 해시만 저장

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private String rawToken; // 메일 발송 시 보여줄 실제 코드

    private PasswordResetToken(Account account, String rawToken, String tokenHash, LocalDateTime expiresAt) {
        this.account = account;
        this.rawToken = rawToken;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    /** 토큰 발급 (6자리 숫자) */
    public static PasswordResetToken issue(Account account, long minutes) {
        // 000000 ~ 999999 범위의 6자리 문자열
        String raw = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        String hash = sha256Hex(raw);
        return new PasswordResetToken(account, raw, hash, LocalDateTime.now().plusMinutes(minutes));
    }

    public boolean isUsable() {
        return usedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    private static String sha256Hex(String s) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}