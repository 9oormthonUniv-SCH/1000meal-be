package com._1000meal.holiday.repository;

import com._1000meal.holiday.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByDate(LocalDate date);
    Optional<Holiday> findByDate(LocalDate date);
    List<Holiday> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

    @Query("""
            select case when count(h) > 0 then true else false end
            from Holiday h
            where h.date = :date
            """)
    boolean isHoliday(LocalDate date);
}

