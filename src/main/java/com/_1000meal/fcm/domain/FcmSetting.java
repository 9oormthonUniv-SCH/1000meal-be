package com._1000meal.fcm.domain;

import com._1000meal.auth.model.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fcm_settings",
        uniqueConstraints = @UniqueConstraint(name = "uk_fcm_settings_account", columnNames = "account_id")
)
@Getter
@NoArgsConstructor
public class FcmSetting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false)
    private boolean enabled; // 기본 false 권장

    @Column(length = 512)
    private String token; // 추후 멀티 디바이스면 별도 테이블로 분리

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static FcmSetting create(Account account) {
        FcmSetting s = new FcmSetting();
        s.account = account;
        s.enabled = false;
        s.createdAt = LocalDateTime.now();
        s.updatedAt = LocalDateTime.now();
        return s;
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void upsertToken(String token) {
        this.token = token;
        this.updatedAt = LocalDateTime.now();
    }

    public void clearToken() {
        this.token = null;
        this.updatedAt = LocalDateTime.now();
    }
}