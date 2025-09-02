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

    private Integer stock;

    @OneToMany(mappedBy = "dailyMenu", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Menu> menus = new ArrayList<>();

    @Builder
    public DailyMenu(WeeklyMenu weeklyMenu, LocalDate date) {
        this.weeklyMenu = weeklyMenu;
        this.date = date;
        this.dayOfWeek = date != null ? date.getDayOfWeek() : null;
        this.isOpen = true; // 메뉴가 추가되기 전까지는 닫힘
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
                .menus(menuNames)
                .stock(this.getStock())
                .build();
    }

    public void deductStock(Integer value) {
        this.stock -= value;
    }

//    /** 기존 Menu 엔티티를 추가 */
//    public void addMenu(Menu menu) {
//        if (menu == null) return;
//        // 이미 다른 DailyMenu에 붙어있으면 떼어내기
//        if (menu.getDailyMenu() != null && menu.getDailyMenu() != this) {
//            menu.getDailyMenu().removeMenu(menu);
//        }
//        this.menus.add(menu);
//        // Menu#setDailyMenu 는 protected이므로 같은 패키지에서 호출 가능
//        menu.setDailyMenu(this);
//        this.isOpen = true;
//    }
//
//    /** 메뉴명만으로 간편 추가 */
//    public void addMenu(String name) {
//        addMenu(Menu.builder().name(name).build());
//    }
//
//    /** 여러 개 한 번에 추가 */
//    public void addMenus(List<Menu> menus) {
//        if (menus == null) return;
//        menus.forEach(this::addMenu);
//    }
//
//    /** 메뉴명 리스트로 한 번에 추가 */
//    public void addMenuNames(List<String> names) {
//        if (names == null) return;
//        names.forEach(this::addMenu);
//    }
//
//    /** 제거 (연관관계 및 isOpen 함께 정리) */
//    public void removeMenu(Menu menu) {
//        if (menu == null) return;
//        if (this.menus.remove(menu)) {
//            // 역방향 끊기
//            menu.setDailyMenu(null);
//            this.isOpen = !this.menus.isEmpty();
//        }
//    }
//
//    /** 날짜 변경 시 요일 일관성 유지 */
//    public void changeDate(LocalDate newDate) {
//        this.date = newDate;
//        this.dayOfWeek = newDate != null ? newDate.getDayOfWeek() : null;
//    }
}
