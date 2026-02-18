package com._1000meal.fcm.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_history_type_account_store_date",
                columnNames = {"type", "account_id", "store_id", "menu_group_id", "sent_date", "week_key"}
        ),
        indexes = @Index(name = "idx_notification_history_account_sent_date", columnList = "account_id, sent_date")
)
@Getter
@NoArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "menu_group_id")
    private Long menuGroupId;

    @Column(name = "week_key", length = 20)
    private String weekKey;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static NotificationHistory create(
            NotificationType type,
            Long accountId,
            Long storeId,
            Long menuGroupId,
            LocalDate sentDate,
            String weekKey
    ) {
        NotificationHistory h = new NotificationHistory();
        h.type = type;
        h.accountId = accountId;
        h.storeId = storeId;
        h.menuGroupId = menuGroupId;
        h.sentDate = sentDate;
        h.weekKey = weekKey;
        h.createdAt = LocalDateTime.now();
        return h;
    }
}
