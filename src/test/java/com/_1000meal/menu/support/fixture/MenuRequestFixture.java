package com._1000meal.menu.support.fixture;

import com._1000meal.menu.dto.DailyMenuAddRequest;

import java.time.LocalDate;
import java.util.List;

public final class MenuRequestFixture {

    private MenuRequestFixture() {}

    public static DailyMenuAddRequest dailyAdd(LocalDate date, List<String> menus) {
        DailyMenuAddRequest req = new DailyMenuAddRequest();
        req.setDate(date);
        req.setMenus(menus);
        return req;
    }
}