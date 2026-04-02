package com._1000meal.holiday.controller;

import com._1000meal.global.security.JwtAuthenticationFilter;
import com._1000meal.holiday.dto.HolidayResponse;
import com._1000meal.holiday.dto.HolidayUpdateRequest;
import com._1000meal.holiday.service.HolidaySyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = HolidayController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HolidaySyncService holidaySyncService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("연도별 휴일 조회: 200 OK와 목록 반환")
    void getHolidays_returnsOk() throws Exception {
        when(holidaySyncService.getHolidaysByYear(2026)).thenReturn(List.of(
                new HolidayResponse(1L, LocalDate.of(2026, 1, 1), "신정")
        ));

        mockMvc.perform(get("/api/v1/holidays").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("신정"));
    }

    @Test
    @DisplayName("휴일 수정: 서비스 호출 후 200 반환")
    void updateHoliday_callsService() throws Exception {
        when(holidaySyncService.updateHoliday(anyLong(), eq(new HolidayUpdateRequest("변경명"))))
                .thenReturn(new HolidayResponse(1L, LocalDate.of(2026, 1, 1), "변경명"));

        String body = objectMapper.writeValueAsString(new RequestBody("변경명"));

        mockMvc.perform(patch("/api/v1/holidays/{holidayId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("변경명"));

        verify(holidaySyncService).updateHoliday(eq(1L), eq(new HolidayUpdateRequest("변경명")));
    }

    @Test
    @DisplayName("연도별 휴일 동기화: 지정 연도로 서비스 호출")
    void syncHolidays_callsServiceWithYear() throws Exception {
        doNothing().when(holidaySyncService).syncYear(anyInt());

        mockMvc.perform(post("/api/v1/holidays/sync").param("year", "2027"))
                .andExpect(status().isOk());

        verify(holidaySyncService).syncYear(eq(2027));
    }

    private record RequestBody(String name) {
    }
}
