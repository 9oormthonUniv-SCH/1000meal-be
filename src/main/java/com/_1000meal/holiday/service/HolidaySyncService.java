package com._1000meal.holiday.service;

import com._1000meal.holiday.client.HolidayApiClient;
import com._1000meal.holiday.domain.Holiday;
import com._1000meal.holiday.dto.HolidayApiResponse;
import com._1000meal.holiday.dto.HolidayResponse;
import com._1000meal.holiday.dto.HolidayUpdateRequest;
import com._1000meal.holiday.dto.HolidayUpsertRequest;
import com._1000meal.holiday.repository.HolidayRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByYear(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return holidayRepository.findByDateBetweenOrderByDateAsc(start, end).stream()
                .map(HolidayResponse::from)
                .toList();
    }

    @Transactional
    public HolidayResponse createHoliday(HolidayUpsertRequest request) {
        if (holidayRepository.existsByDate(request.date())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 등록된 휴일 날짜입니다.");
        }

        Holiday holiday = Holiday.builder()
                .date(request.date())
                .name(request.name().trim())
                .build();
        return HolidayResponse.from(holidayRepository.save(holiday));
    }

    @Transactional
    public HolidayResponse updateHoliday(Long holidayId, HolidayUpdateRequest request) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "휴일을 찾을 수 없습니다."));
        holiday.updateName(request.name().trim());
        return HolidayResponse.from(holiday);
    }

    @Transactional
    public void deleteHoliday(Long holidayId) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "휴일을 찾을 수 없습니다."));
        holidayRepository.delete(holiday);
    }

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

                holidayRepository.findByDate(date)
                        .ifPresentOrElse(
                                existing -> existing.updateName(item.getDateName()),
                                () -> holidayRepository.save(Holiday.builder()
                                        .date(date)
                                        .name(item.getDateName())
                                        .build())
                        );
            }
        } catch (Exception e) {
            log.error("[HOLIDAY-SYNC] failed for year={}", year, e);
        }

        log.info("[HOLIDAY-SYNC] done year={}", year);
    }
}

