package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_menu_id")
    private DailyMenu dailyMenu;

    @Builder
    public Menu(String name) {
        this.name = name;
    }
}