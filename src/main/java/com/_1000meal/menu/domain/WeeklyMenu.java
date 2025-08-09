package com._1000meal.menu.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 주간 메뉴의 시작 날짜 (예: 2023-10-16)
    private LocalDate startDate;

    // 해당 주간 메뉴의 종료 날짜 (예: 2023-10-22)
    private LocalDate endDate;

    // Store와 OneToOne 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // DailyMenu와 OneToMany 관계
    @OneToMany(mappedBy = "weeklyMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyMenu> dailyMenus = new ArrayList<>();
}