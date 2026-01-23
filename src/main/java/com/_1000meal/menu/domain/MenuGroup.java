package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "menu_group",
        indexes = @Index(name = "idx_menu_group_daily_menu", columnList = "daily_menu_id")
)
public class MenuGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_menu_id", nullable = false)
    private DailyMenu dailyMenu;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer sortOrder;

    @OneToOne(mappedBy = "menuGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MenuGroupStock stock;

    @OneToMany(mappedBy = "menuGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Menu> menus = new ArrayList<>();

    @Builder
    public MenuGroup(DailyMenu dailyMenu, String name, Integer sortOrder) {
        this.dailyMenu = dailyMenu;
        this.name = name;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void initializeStock(int capacity) {
        this.stock = MenuGroupStock.of(this, capacity);
    }

    public void addMenu(Menu menu) {
        this.menus.add(menu);
        menu.setMenuGroup(this);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
