package com._1000meal.holiday.service;

import com._1000meal.holiday.client.HolidayApiClient;
import com._1000meal.holiday.domain.Holiday;
import com._1000meal.holiday.dto.HolidayApiResponse;
import com._1000meal.holiday.repository.HolidayRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 외부 API로부터 연도별 공휴일 정보를 조회해 DB에 저장/갱신하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HolidaySyncService {

    private final HolidayApiClient apiClient;
    private final HolidayRepository holidayRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncYear(int year) {
        log.info("[HOLIDAY-SYNC] start year={}", year);

        try {
            String json = apiClient.getRestHolidaysJson(year);
            HolidayApiResponse resp = objectMapper.readValue(json, HolidayApiResponse.class);

            if (resp.getResponse() == null
                    || resp.getResponse().getBody() == null
                    || resp.getResponse().getBody().getItems() == null
                    || resp.getResponse().getBody().getItems().getItem() == null) {
                log.info("[HOLIDAY-SYNC] no items for year={}", year);
                return;
            }

            for (HolidayApiResponse.Item item : resp.getResponse().getBody().getItems().getItem()) {
                int locdate = item.getLocdate();
                int y = locdate / 10000;
                int m = (locdate / 100) % 100;
                int d = locdate % 100;
                LocalDate date = LocalDate.of(y, m, d);

                if (holidayRepository.existsByDate(date)) {
                    continue;
                }

                Holiday h = Holiday.builder()
                        .date(date)
                        .name(item.getDateName())
                        .build();
                holidayRepository.save(h);
            }
        } catch (Exception e) {
            log.error("[HOLIDAY-SYNC] failed for year={}", year, e);
        }

        log.info("[HOLIDAY-SYNC] done year={}", year);
    }
}

