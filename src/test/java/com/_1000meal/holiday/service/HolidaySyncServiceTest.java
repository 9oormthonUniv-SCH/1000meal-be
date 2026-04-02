package com._1000meal.holiday.service;

import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.holiday.client.HolidayApiClient;
import com._1000meal.holiday.domain.Holiday;
import com._1000meal.holiday.dto.HolidayResponse;
import com._1000meal.holiday.dto.HolidayUpdateRequest;
import com._1000meal.holiday.dto.HolidayUpsertRequest;
import com._1000meal.holiday.repository.HolidayRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidaySyncServiceTest {

    @Mock
    private HolidayApiClient apiClient;
    @Mock
    private HolidayRepository holidayRepository;

    private HolidaySyncService holidaySyncService;

    @BeforeEach
    void setUp() {
        holidaySyncService = new HolidaySyncService(apiClient, holidayRepository, new ObjectMapper());
    }

    @Test
    @DisplayName("휴일 등록: 같은 날짜가 이미 있으면 CONFLICT 예외")
    void createHoliday_duplicateDate_throwsConflict() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        HolidayUpsertRequest request = new HolidayUpsertRequest(date, "신정");
        when(holidayRepository.existsByDate(date)).thenReturn(true);

        CustomException ex = assertThrows(CustomException.class, () -> holidaySyncService.createHoliday(request));

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCodeIfs());
        verify(holidayRepository, never()).save(any(Holiday.class));
    }

    @Test
    @DisplayName("휴일 이름 수정: ID가 존재하면 이름이 변경된다")
    void updateHoliday_updatesName() {
        Holiday holiday = Holiday.builder()
                .id(1L)
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build();
        when(holidayRepository.findById(1L)).thenReturn(Optional.of(holiday));

        HolidayResponse response = holidaySyncService.updateHoliday(1L, new HolidayUpdateRequest("삼일절(수정)"));

        assertEquals(1L, response.id());
        assertEquals("삼일절(수정)", response.name());
        assertEquals("삼일절(수정)", holiday.getName());
    }

    @Test
    @DisplayName("연도 동기화: 없는 날짜는 insert, 있는 날짜는 name update")
    void syncYear_upsertByDate() {
        String json = """
                {
                  "response": {
                    "body": {
                      "items": {
                        "item": [
                          {"dateName":"신정","locdate":20260101},
                          {"dateName":"삼일절(신규명칭)","locdate":20260301}
                        ]
                      }
                    }
                  }
                }
                """;
        when(apiClient.getRestHolidaysJson(2026)).thenReturn(json);

        Holiday existing = Holiday.builder()
                .id(10L)
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build();

        when(holidayRepository.findByDate(LocalDate.of(2026, 1, 1))).thenReturn(Optional.empty());
        when(holidayRepository.findByDate(LocalDate.of(2026, 3, 1))).thenReturn(Optional.of(existing));

        holidaySyncService.syncYear(2026);

        ArgumentCaptor<Holiday> captor = ArgumentCaptor.forClass(Holiday.class);
        verify(holidayRepository, times(1)).save(captor.capture());
        assertEquals(LocalDate.of(2026, 1, 1), captor.getValue().getDate());
        assertEquals("신정", captor.getValue().getName());
        assertEquals("삼일절(신규명칭)", existing.getName());
    }

    @Test
    @DisplayName("연도 조회: 시작일~종료일 범위로 정렬 조회한다")
    void getHolidaysByYear_loadsRange() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        when(holidayRepository.findByDateBetweenOrderByDateAsc(start, end)).thenReturn(List.of(
                Holiday.builder().id(1L).date(start).name("신정").build(),
                Holiday.builder().id(2L).date(LocalDate.of(2026, 3, 1)).name("삼일절").build()
        ));

        List<HolidayResponse> responses = holidaySyncService.getHolidaysByYear(2026);

        assertEquals(2, responses.size());
        assertEquals("신정", responses.get(0).name());
        assertEquals("삼일절", responses.get(1).name());
    }
}
