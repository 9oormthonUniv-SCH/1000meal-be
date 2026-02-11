package com._1000meal.menu.controller;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.fcm.service.FcmPushService;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class StoreMenuGroupControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StoreRepository storeRepository;
    @Autowired MenuGroupRepository menuGroupRepository;
    @Autowired MenuGroupStockRepository menuGroupStockRepository;

    @MockBean CurrentAccountProvider currentAccountProvider;
    @MockBean FcmPushService fcmPushService;

    @Test
    @DisplayName("등록→재고수정→차감: stock 30 도달 + 30 알림 1회")
    void registerUpdateDeductFlow() throws Exception {
        Store store = storeRepository.save(Store.builder()
                .name("store")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(true)
                .remain(100)
                .hours("08:00 ~ 소진 시")
                .lat(0.0)
                .lng(0.0)
                .imageUrl("img")
                .build());

        MenuGroup group = menuGroupRepository.save(MenuGroup.builder()
                .store(store)
                .name("groupA")
                .sortOrder(1)
                .isDefault(false)
                .build());

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(store.getId());

        String menusPayload = objectMapper.writeValueAsString(
                new com._1000meal.menu.dto.MenuUpdateRequest(List.of("menu1", "menu2"))
        );

        mockMvc.perform(post("/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/menus", store.getId(), group.getId())
                        .param("date", LocalDate.now().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(menusPayload))
                .andExpect(status().isOk());

        String stockPayload = objectMapper.writeValueAsString(
                new com._1000meal.menu.dto.StockUpdateRequest(100)
        );

        mockMvc.perform(post("/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/stock", store.getId(), group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockPayload))
                .andExpect(status().isOk());

        for (int i = 0; i < 7; i++) {
            mockMvc.perform(post("/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/deduct", store.getId(), group.getId())
                            .param("deductionUnit", "MULTI_TEN"))
                    .andExpect(status().isOk());
        }

        MenuGroupStock stock = menuGroupStockRepository.findByMenuGroupId(group.getId()).orElseThrow();
        assertEquals(30, stock.getStock());

        verify(fcmPushService, times(1)).sendLowStock30Notification(
                eq(store.getId()),
                eq(store.getName()),
                eq(group.getId()),
                eq(group.getName()),
                eq(30)
        );
    }

    @Test
    @DisplayName("storeId/groupId 불일치면 404")
    void mismatchStoreGroupReturns404() throws Exception {
        Store store1 = storeRepository.save(Store.builder()
                .name("store1")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(true)
                .remain(100)
                .hours("08:00 ~ 소진 시")
                .lat(0.0)
                .lng(0.0)
                .imageUrl("img")
                .build());

        Store store2 = storeRepository.save(Store.builder()
                .name("store2")
                .address("addr2")
                .phone("010-0000-0001")
                .description("desc2")
                .isOpen(true)
                .remain(100)
                .hours("08:00 ~ 소진 시")
                .lat(0.0)
                .lng(0.0)
                .imageUrl("img2")
                .build());

        MenuGroup group = menuGroupRepository.save(MenuGroup.builder()
                .store(store1)
                .name("groupA")
                .sortOrder(1)
                .isDefault(false)
                .build());

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(store2.getId());

        String stockPayload = objectMapper.writeValueAsString(
                new com._1000meal.menu.dto.StockUpdateRequest(50)
        );

        mockMvc.perform(post("/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/stock", store2.getId(), group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockPayload))
                .andExpect(status().isNotFound());

        verify(fcmPushService, never()).sendLowStock30Notification(anyLong(), any(), anyLong(), any(), anyInt());
    }
}
