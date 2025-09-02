package com._1000meal.favoriteMenu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    FavoriteMenuGroup group;

    @Builder
    public FavoriteMenu(String name, FavoriteMenuGroup group) {
        this.name = name;
        this.group = group;
    }
}



