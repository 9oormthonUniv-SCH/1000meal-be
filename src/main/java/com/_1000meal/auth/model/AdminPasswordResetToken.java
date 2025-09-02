package com._1000meal.auth.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_password_reset_tokens",
        indexes = {
                @Index(name = "idx_aprt_account_id", columnList = "account_id"),
                @Index(name = "idx_aprt_token", columnList = "token", unique = true)
        })
@Getter
@NoArgsConstructor
public class AdminPasswordResetToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static AdminPasswordResetToken create(Account account, int minutesToExpire) {
        AdminPasswordResetToken t = new AdminPasswordResetToken();
        t.account = account;
        t.token = UUID.randomUUID().toString().replace("-", "");
        t.expiresAt = LocalDateTime.now().plusMinutes(minutesToExpire);
        t.used = false;
        return t;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markUsed() {
        this.used = true;
    }
}