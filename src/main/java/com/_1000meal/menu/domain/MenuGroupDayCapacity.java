package com._1000meal.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.DayOfWeek;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "menu_group_capacity_by_day",
        uniqueConstraints = @UniqueConstraint(name = "uk_menu_group_capacity_by_day", columnNames = {"menu_group_id", "day_of_week"})
)
public class MenuGroupDayCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_group_id", nullable = false)
    private MenuGroup menuGroup;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private Integer capacity;

    public static MenuGroupDayCapacity create(MenuGroup menuGroup, DayOfWeek dayOfWeek, int capacity) {
        MenuGroupDayCapacity entity = new MenuGroupDayCapacity();
        entity.menuGroup = menuGroup;
        entity.dayOfWeek = dayOfWeek;
        entity.capacity = capacity;
        return entity;
    }

    public void updateCapacity(int capacity) {
        this.capacity = capacity;
    }
}
