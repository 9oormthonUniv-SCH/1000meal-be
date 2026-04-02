package com._1000meal.holiday.service;

import com._1000meal.holiday.repository.HolidayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayScheduleGuardTest {

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private HolidayScheduleGuard holidayScheduleGuard;

    @Test
    @DisplayName("휴일이면 shouldSkip은 true")
    void shouldSkip_returnsTrue_whenHoliday() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        when(holidayRepository.isHoliday(date)).thenReturn(true);

        boolean result = holidayScheduleGuard.shouldSkip("STOCK_RESET", date);

        assertTrue(result);
    }

    @Test
    @DisplayName("휴일이 아니면 shouldSkip은 false")
    void shouldSkip_returnsFalse_whenNotHoliday() {
        LocalDate date = LocalDate.of(2026, 1, 2);
        when(holidayRepository.isHoliday(date)).thenReturn(false);

        boolean result = holidayScheduleGuard.shouldSkip("STOCK_RESET", date);

        assertFalse(result);
    }
}
