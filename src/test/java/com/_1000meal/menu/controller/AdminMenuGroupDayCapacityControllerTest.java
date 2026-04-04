package com._1000meal.menu.controller;

import com._1000meal.global.security.JwtAuthenticationFilter;
import com._1000meal.menu.dto.MenuGroupDayCapacityAdminResponse;
import com._1000meal.menu.service.MenuGroupDayCapacityService;
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

import java.time.DayOfWeek;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminMenuGroupDayCapacityController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminMenuGroupDayCapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MenuGroupDayCapacityService menuGroupDayCapacityService;

    @Test
    @DisplayName("PATCH capacity-by-day: 요일별 수량 수정 시 200과 본문을 반환한다")
    void patch_updatesCapacities() throws Exception {
        MenuGroupDayCapacityAdminResponse response = MenuGroupDayCapacityAdminResponse.of(
                1L,
                2L,
                List.of()
        );
        when(menuGroupDayCapacityService.updateCapacitiesForAdmin(eq(1L), eq(2L), any()))
                .thenReturn(response);

        String body = """
                {
                  "capacities": [
                    { "dayOfWeek": "MONDAY", "capacity": 80 },
                    { "dayOfWeek": "FRIDAY", "capacity": 60 }
                  ]
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/stores/1/menus/daily/groups/2/capacity-by-day")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeId").value(1))
                .andExpect(jsonPath("$.data.groupId").value(2));
    }

    @Test
    @DisplayName("PATCH capacity-by-day: capacities가 비어 있으면 검증 오류 응답")
    void patch_emptyCapacities_validationError() throws Exception {
        String body = "{ \"capacities\": [] }";

        mockMvc.perform(patch("/api/v1/admin/stores/1/menus/daily/groups/2/capacity-by-day")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("COMMON_400_VALIDATION"));
    }
}
