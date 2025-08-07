package com._1000meal.menu.repository;

import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {
    Optional<WeeklyMenu> findByStore(Store store);
}