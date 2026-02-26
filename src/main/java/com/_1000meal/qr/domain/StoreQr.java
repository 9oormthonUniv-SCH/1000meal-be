package com._1000meal.qr.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "store_qr",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_qr_store_group", columnNames = {"store_id", "menu_group_id"}),
                @UniqueConstraint(name = "uk_store_qr_token", columnNames = "qr_token")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreQr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "qr_token", length = 100, nullable = false)
    private String qrToken;

    @Column(name = "menu_group_id")
    private Long menuGroupId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private StoreQr(Store store, String qrToken, boolean isActive) {
        this.store = store;
        this.qrToken = qrToken;
        this.isActive = isActive;
    }

    public static StoreQr create(Store store, String qrToken) {
        return new StoreQr(store, qrToken, true);
    }

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
