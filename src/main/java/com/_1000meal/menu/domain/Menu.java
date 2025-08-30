package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_menu_id", nullable = false)
    private DailyMenu dailyMenu;

    // 이름만 받는 빌더 (FK는 편의 메서드로 세팅)
    @Builder
    public Menu(String name) {
        this.name = name;
    }

    // 연관관계 편의 메서드 (필요 시 public, 기본은 패키지/프로텍티드 권장)
    public void setDailyMenu(DailyMenu dailyMenu) {
        this.dailyMenu = dailyMenu;
    }
}