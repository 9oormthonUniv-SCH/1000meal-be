package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_menu_id")
    private WeeklyMenu weeklyMenu;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private boolean isOpen;

    // DailyMenu와 Menu의 일대다 관계 설정
    @OneToMany(mappedBy = "dailyMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Builder
    public DailyMenu(WeeklyMenu weeklyMenu, LocalDate date, DayOfWeek dayOfWeek) {
        this.weeklyMenu = weeklyMenu;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.isOpen = false; // 메뉴가 추가되기 전까지는 false로 설정
    }

    // 비즈니스 로직
    public void addMenu(Menu menu) {
        this.menus.add(menu);
        menu.setDailyMenu(this); // 양방향 연관관계 설정
        this.isOpen = true;      // 메뉴가 추가되면 isOpen을 true로 변경
    }

    public void removeMenu(Menu menu) {
        this.menus.remove(menu);
        this.isOpen = !this.menus.isEmpty(); // 메뉴가 모두 사라지면 isOpen을 false로 변경
    }
}