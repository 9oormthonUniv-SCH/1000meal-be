package com._1000meal.favoriteMenu.repository;

import com._1000meal.favoriteMenu.domain.FavoriteMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteMenuRepository extends JpaRepository<FavoriteMenu, Long>{
    long deleteByGroup_Id(Long groupId);

    @Query("""
      select fm from FavoriteMenu fm
      join fm.group g
      where g.store.id = :storeId
      order by g.id asc, fm.id asc
    """)
    List<FavoriteMenu> findAllByStore(@Param("storeId") Long storeId);
}
