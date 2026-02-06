package com._1000meal.menu.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "default_group_menu",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_default_group_menu",
                columnNames = {"menu_group_id", "menu_name", "start_date", "end_date"}
        ),
        indexes = {
                @Index(name = "idx_default_group_menu_group", columnList = "menu_group_id"),
                @Index(name = "idx_default_group_menu_range", columnList = "menu_group_id, start_date, end_date")
        }
)
public class DefaultGroupMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_group_id", nullable = false)
    private MenuGroup menuGroup;

    @Column(name = "menu_name", nullable = false, length = 80)
    private String menuName;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_by_account_id")
    private Long createdByAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public DefaultGroupMenu(
            Store store,
            MenuGroup menuGroup,
            String menuName,
            boolean active,
            LocalDate startDate,
            LocalDate endDate,
            Long createdByAccountId
    ) {
        this.store = store;
        this.menuGroup = menuGroup;
        this.menuName = menuName;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdByAccountId = createdByAccountId;
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

    public void close(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void deactivate(LocalDate endDate) {
        this.endDate = endDate;
        this.active = false;
    }

    public boolean isPinnedOn(LocalDate date) {
        if (date == null) {
            return false;
        }
        if (!active) {
            return false;
        }
        if (date.isBefore(startDate)) {
            return false;
        }
        return endDate == null || endDate.isAfter(date);
    }

    public boolean isCarryoverOn(LocalDate date) {
        if (date == null || endDate == null) {
            return false;
        }
        if (!active) {
            return false;
        }
        return !date.isBefore(startDate) && endDate.isEqual(date);
    }

    public void setActiveRule(LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            this.startDate = startDate;
        }
        this.endDate = endDate;
        this.active = true;
    }

    public void activate(LocalDate startDate, LocalDate endDate) {
        setActiveRule(startDate, endDate);
    }
}
