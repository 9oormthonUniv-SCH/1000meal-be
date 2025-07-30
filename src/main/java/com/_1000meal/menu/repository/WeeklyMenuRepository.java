package com._1000meal.menu.repository;

import com._1000meal.menu.domain.WeeklyMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {
}