package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "group_daily_menu",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_daily_menu",
                columnNames = {"menu_group_id", "date"}
        )
)
public class GroupDailyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_group_id", nullable = false)
    private MenuGroup menuGroup;

    @Column(nullable = false)
    private LocalDate date;

    @ElementCollection
    @CollectionTable(
            name = "group_daily_menu_item",
            joinColumns = @JoinColumn(name = "group_daily_menu_id")
    )
    @Column(name = "name", nullable = false, length = 80)
    private List<String> menuNames = new ArrayList<>();

    @Builder
    public GroupDailyMenu(MenuGroup menuGroup, LocalDate date) {
        this.menuGroup = menuGroup;
        this.date = date;
    }

    public void replaceMenus(List<String> newMenuNames) {
        this.menuNames.clear();
        this.menuNames.addAll(newMenuNames);
    }
}
