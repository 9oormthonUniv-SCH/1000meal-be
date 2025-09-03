package com._1000meal.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "password_reset_token",
        indexes = {
                @Index(name = "idx_prt_token", columnList = "token", unique = true),
                @Index(name = "idx_prt_account", columnList = "account_id")
        }
)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★ Account 기준으로 통일
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, unique = true, length = 191)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private PasswordResetToken(Account account, String token, LocalDateTime expiresAt) {
        this.account = account;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    // 발급 팩토리
    public static PasswordResetToken issue(Account account, long minutes) {
        String t = UUID.randomUUID().toString().replace("-", "");
        return new PasswordResetToken(account, t, LocalDateTime.now().plusMinutes(minutes));
    }

    // 사용 가능 여부
    public boolean isUsable() {
        return usedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    // 1회성 사용 처리
    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }

    // (옵션) createdAt 자동 세팅 보강
    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}