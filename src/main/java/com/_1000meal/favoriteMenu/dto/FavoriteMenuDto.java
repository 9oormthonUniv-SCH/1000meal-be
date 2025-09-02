package com._1000meal.favoriteMenu.dto;
import com._1000meal.favoriteMenu.domain.FavoriteMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteMenuDto {
    private Long id;
    private Long groupId;
    private String name;

    public static FavoriteMenuDto from(FavoriteMenu e) {
        return FavoriteMenuDto.builder()
                .id(e.getId())
                .groupId(e.getGroup() != null ? e.getGroup().getId() : null)
                .name(e.getName())
                .build();
    }
}