package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.*;


import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyMenu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private WeeklyMenu weeklyMenu;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private boolean isOpen;

    // 메뉴를 쉼표로 구분된 문자열로 저장
    private String menuTexts;
}

