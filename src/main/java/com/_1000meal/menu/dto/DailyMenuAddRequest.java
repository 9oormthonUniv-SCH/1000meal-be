package com._1000meal.menu.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMenuAddRequest {

    private LocalDate date;
    private List<String> menus;
}
