package com._1000meal.holiday.service;

import com._1000meal.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayScheduleGuard {

    private final HolidayRepository holidayRepository;

    public boolean shouldSkip(String jobName, LocalDate date) {
        boolean holiday = holidayRepository.isHoliday(date);
        if (holiday) {
            log.info("[SCHEDULER][{}] holiday skip. date={}", jobName, date);
            return true;
        }
        return false;
    }
}
