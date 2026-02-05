package com._1000meal.menu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultMenuRequest {

    @NotBlank(message = "메뉴명은 비어 있을 수 없습니다.")
    private String menuName;
}
