package com._1000meal.menu.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "menu_presets",
        indexes = @Index(name = "idx_menu_presets_store", columnList = "store_id")
)
public class MenuPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "created_by_account_id", nullable = false)
    private Long createdByAccountId;

    @ElementCollection
    @CollectionTable(
            name = "menu_preset_items",
            joinColumns = @JoinColumn(name = "preset_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "name", nullable = false, length = 200)
    private List<String> menus = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MenuPreset(Store store, Long createdByAccountId, List<String> menus) {
        this.store = store;
        this.createdByAccountId = createdByAccountId;
        if (menus != null) {
            this.menus.addAll(menus);
        }
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void replaceMenus(List<String> newMenus) {
        this.menus.clear();
        if (newMenus != null) {
            this.menus.addAll(newMenus);
        }
    }
}
