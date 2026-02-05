package com._1000meal.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {

    @NotEmpty(message = "메뉴는 최소 1개 이상 필요합니다.")
    private List<@NotBlank(message = "메뉴명은 비어 있을 수 없습니다.") String> menus;
}
