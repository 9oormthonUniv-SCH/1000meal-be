package com._1000meal.fcm.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "weekly_menu_notification_state",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weekly_menu_notification_state",
                columnNames = {"store_id", "menu_group_id", "week_key"}
        ),
        indexes = @Index(
                name = "idx_weekly_menu_notification_state_status_week_key",
                columnList = "status, week_key"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyMenuNotificationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "menu_group_id", nullable = false)
    private Long menuGroupId;

    @Column(name = "week_key", nullable = false, length = 20)
    private String weekKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WeeklyMenuNotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WeeklyMenuNotificationState create(
            Long storeId,
            Long menuGroupId,
            String weekKey,
            WeeklyMenuNotificationStatus status
    ) {
        WeeklyMenuNotificationState state = new WeeklyMenuNotificationState();
        state.storeId = storeId;
        state.menuGroupId = menuGroupId;
        state.weekKey = weekKey;
        state.status = status;
        state.createdAt = LocalDateTime.now();
        state.updatedAt = LocalDateTime.now();
        return state;
    }

    public void changeStatus(WeeklyMenuNotificationStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
