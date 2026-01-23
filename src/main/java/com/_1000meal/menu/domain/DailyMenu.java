package com._1000meal.menu.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com._1000meal.menu.dto.DailyMenuDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_menu_id")
    private WeeklyMenu weeklyMenu;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private boolean isOpen;

    private boolean isHoliday;   // ✅ 휴무 여부 추가

    private Integer stock;

    @OneToMany(mappedBy = "dailyMenu", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "dailyMenu", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private final List<MenuGroup> menuGroups = new ArrayList<>();

    @Builder
    public DailyMenu(WeeklyMenu weeklyMenu, LocalDate date) {
        this.weeklyMenu = weeklyMenu;
        this.date = date;
        this.dayOfWeek = date != null ? date.getDayOfWeek() : null;
        this.isOpen = true;   // 기본값: 운영 중
        this.isHoliday = false; // 기본값: 휴무 아님
        this.stock = 100;
    }

    public void updateStock(Integer stock) {
        this.stock = stock;
    }

    public DailyMenuDto toDto() {
        List<String> menuNames = this.getMenus().stream()
                .map(Menu::getName)
                .collect(Collectors.toList());

        DayOfWeek dow = (this.getDate() != null)
                ? this.getDate().getDayOfWeek()
                : this.getDayOfWeek();

        return DailyMenuDto.builder()
                .id(this.getId())
                .date(this.getDate())
                .dayOfWeek(dow)
                .isOpen(this.isOpen())
                .isHoliday(this.isHoliday)   // ✅ DTO로 전달
                .menus(menuNames)
                .stock(this.getStock())
                .build();
    }

    public void deductStock(Integer value) {
        this.stock -= value;
    }

    public void toggleIsOpen() {
        isOpen = !isOpen;
    }

    public void markHoliday(boolean holiday) {
        this.isHoliday = holiday;
        if (holiday) {
            this.isOpen = false;  // 휴무일이면 자동으로 닫힘 처리
            this.stock = 0;       // 재고도 0으로 처리
        }
    }

    /**
     * 모든 그룹의 재고 합계를 반환
     */
    public int getTotalGroupStock() {
        return menuGroups.stream()
                .filter(g -> g.getStock() != null)
                .mapToInt(g -> g.getStock().getStock())
                .sum();
    }

    public void addMenuGroup(MenuGroup menuGroup) {
        this.menuGroups.add(menuGroup);
    }
}

