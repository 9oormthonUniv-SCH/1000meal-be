package com._1000meal.favoriteMenu.repository;

import com._1000meal.favoriteMenu.domain.FavoriteMenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


public interface FavoriteMenuGroupRepository extends JpaRepository<FavoriteMenuGroup, Long> {

    @Query("""
        select distinct g
        from FavoriteMenuGroup g
        left join fetch g.menus m
        where g.store.id = :storeId
        order by g.id asc, m.id asc
    """)
    List<FavoriteMenuGroup> findByStoreIdWithMenus(@Param("storeId") Long storeId);

    @Query("select g.id from FavoriteMenuGroup g where g.store.id = :storeId and g.id in :ids")
    List<Long> findOwnedIds(@Param("storeId") Long storeId, @Param("ids") Collection<Long> ids);
}