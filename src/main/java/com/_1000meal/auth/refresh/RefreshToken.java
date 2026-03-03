package com._1000meal.auth.refresh;

import com._1000meal.auth.model.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token_account_id", columnList = "account_id"),
                @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_refresh_token_expires", columnList = "expires_at")
        })
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    private RefreshToken(
            Account account,
            String tokenHash,
            LocalDateTime expiresAt,
            String deviceId,
            String userAgent,
            String ipAddress
    ) {
        this.account = account;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public static RefreshToken issue(
            Account account,
            String tokenHash,
            LocalDateTime expiresAt,
            String deviceId,
            String userAgent,
            String ipAddress
    ) {
        return new RefreshToken(account, tokenHash, expiresAt, deviceId, userAgent, ipAddress);
    }

    public void markUsed(LocalDateTime now) {
        this.lastUsedAt = now;
    }

    public void revoke(LocalDateTime now) {
        this.revokedAt = now;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
