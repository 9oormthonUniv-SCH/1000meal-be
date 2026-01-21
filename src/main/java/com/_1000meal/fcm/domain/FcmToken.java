package com._1000meal.fcm.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fcm_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fcm_tokens_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_fcm_tokens_account_active", columnList = "account_id, active")
        }
)
@Getter
@NoArgsConstructor
public class FcmToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FcmPlatform platform;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static FcmToken create(Long accountId, String token, FcmPlatform platform) {
        FcmToken t = new FcmToken();
        t.accountId = accountId;
        t.token = token;
        t.platform = platform;
        t.active = true;
        t.createdAt = LocalDateTime.now();
        t.updatedAt = LocalDateTime.now();
        return t;
    }

    public void relink(Long accountId, FcmPlatform platform) {
        this.accountId = accountId;
        this.platform = platform;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
}