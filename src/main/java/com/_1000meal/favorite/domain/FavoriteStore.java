package com._1000meal.favorite.domain;

import com._1000meal.auth.model.Account;
import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "favorite_store",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_favorite_store_account_store", columnNames = {"account_id", "store_id"})
        },
        indexes = {
                @Index(name = "idx_favorite_store_store", columnList = "store_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private FavoriteStore(Account account, Store store) {
        this.account = account;
        this.store = store;
    }

    public static FavoriteStore of(Account account, Store store) {
        return new FavoriteStore(account, store);
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}