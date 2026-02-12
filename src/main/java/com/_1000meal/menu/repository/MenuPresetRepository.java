package com._1000meal.menu.repository;

import com._1000meal.menu.domain.MenuPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuPresetRepository extends JpaRepository<MenuPreset, Long> {

    @Query("SELECT DISTINCT mp FROM MenuPreset mp " +
            "LEFT JOIN FETCH mp.menus " +
            "WHERE mp.store.id = :storeId " +
            "ORDER BY mp.createdAt DESC, mp.id DESC")
    List<MenuPreset> findByStoreIdWithMenus(@Param("storeId") Long storeId);

    @Query("SELECT mp FROM MenuPreset mp " +
            "LEFT JOIN FETCH mp.menus " +
            "WHERE mp.id = :presetId AND mp.store.id = :storeId")
    Optional<MenuPreset> findByIdAndStoreIdWithMenus(
            @Param("presetId") Long presetId,
            @Param("storeId") Long storeId
    );

    Optional<MenuPreset> findByIdAndStoreId(Long presetId, Long storeId);
}
