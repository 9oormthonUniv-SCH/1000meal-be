package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.*;


import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "hiddenBuilder")
@Entity
public class DailyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private WeeklyMenu weeklyMenu;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private boolean isOpen;

    private String menuTexts;

    public static DailyMenuBuilder builder() {
        return hiddenBuilder().isOpen(false); // 기본값 false로
    }

    @PostLoad @PostPersist @PostUpdate
    private void updateIsOpen() {
        this.isOpen = (menuTexts != null && !menuTexts.trim().isEmpty());
    }

    @PrePersist
    @PreUpdate
    private void prePersistOrUpdate() {
        this.isOpen = (menuTexts != null && !menuTexts.trim().isEmpty());
    }
}


