package com._1000meal.menu.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "weeklyMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyMenu> dailyMenus = new ArrayList<>();
}