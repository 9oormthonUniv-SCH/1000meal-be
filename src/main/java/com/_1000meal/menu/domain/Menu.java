package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter(AccessLevel.PROTECTED) // DailyMenu에서만 변경 가능하도록
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 메뉴 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_menu_id") // 외래키
    private DailyMenu dailyMenu;

    @Builder
    public Menu(String name) {
        this.name = name;
    }
}