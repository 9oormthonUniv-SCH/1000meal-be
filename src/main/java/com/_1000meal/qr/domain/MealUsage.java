package com._1000meal.qr.domain;

import com._1000meal.auth.model.Account;
import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "meal_usage",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meal_usage_user_date", columnNames = {"user_id", "used_date"})
        },
        indexes = {
                @Index(name = "idx_meal_usage_used_date", columnList = "used_date"),
                @Index(name = "idx_meal_usage_store_date", columnList = "store_id, used_date"),
                @Index(name = "idx_meal_usage_date_store_group", columnList = "used_date, store_id, menu_group_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "menu_group_id")
    private Long menuGroupId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @Column(name = "used_date", nullable = false)
    private LocalDate usedDate;

    @Column(name = "dept_snapshot", length = 100, nullable = false)
    private String deptSnapshot;

    @Column(name = "student_no_snapshot", length = 50, nullable = false)
    private String studentNoSnapshot;

    @Column(name = "name_snapshot", length = 50, nullable = false)
    private String nameSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private MealUsage(Account user, Store store, Long menuGroupId, LocalDateTime usedAt, LocalDate usedDate,
                      String deptSnapshot, String studentNoSnapshot, String nameSnapshot) {
        this.user = user;
        this.store = store;
        this.menuGroupId = menuGroupId;
        this.usedAt = usedAt;
        this.usedDate = usedDate;
        this.deptSnapshot = deptSnapshot;
        this.studentNoSnapshot = studentNoSnapshot;
        this.nameSnapshot = nameSnapshot;
    }

    public static MealUsage create(Account user, Store store, Long menuGroupId, LocalDateTime usedAt, LocalDate usedDate,
                                   String deptSnapshot, String studentNoSnapshot, String nameSnapshot) {
        return new MealUsage(user, store, menuGroupId, usedAt, usedDate, deptSnapshot, studentNoSnapshot, nameSnapshot);
    }

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
