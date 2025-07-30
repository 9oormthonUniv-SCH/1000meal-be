package com._1000meal.menu.repository;

import com._1000meal.menu.domain.DailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {
}
