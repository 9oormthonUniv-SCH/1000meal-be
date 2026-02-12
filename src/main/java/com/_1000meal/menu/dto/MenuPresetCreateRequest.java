package com._1000meal.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MenuPresetCreateRequest {

    @NotEmpty(message = "硫붾돱??理쒖냼 1媛??댁긽 ?꾩슂?⑸땲??")
    private List<@NotBlank(message = "硫붾돱紐낆? 鍮꾩뼱 ?덉쓣 ???놁뒿?덈떎.") String> menus;
}
