package com._1000meal.menu.support.fixture;


import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.WeeklyMenu;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

public final class MenuEntityFixture {

    private MenuEntityFixture() {}

    public static WeeklyMenu weeklyMenu(LocalDate start, LocalDate end) {
        WeeklyMenu weekly = mock(WeeklyMenu.class);
        when(weekly.getStartDate()).thenReturn(start);
        when(weekly.getEndDate()).thenReturn(end);
        when(weekly.getId()).thenReturn(1L);
        return weekly;
    }

    public static DailyMenu dailyMenu(LocalDate date) {
        DailyMenu dm = mock(DailyMenu.class);
        when(dm.getDate()).thenReturn(date);
        when(dm.getId()).thenReturn(10L);
        return dm;
    }
}