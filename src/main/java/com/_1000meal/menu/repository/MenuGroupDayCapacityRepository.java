package com._1000meal.menu.repository;

import com._1000meal.menu.domain.MenuGroupDayCapacity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface MenuGroupDayCapacityRepository extends JpaRepository<MenuGroupDayCapacity, Long> {

    Optional<MenuGroupDayCapacity> findByMenuGroupIdAndDayOfWeek(Long menuGroupId, DayOfWeek dayOfWeek);
}
