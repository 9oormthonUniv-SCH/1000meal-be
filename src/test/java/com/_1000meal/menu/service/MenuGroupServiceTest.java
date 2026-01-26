package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.*;
import com._1000meal.menu.dto.DailyMenuWithGroupsDto;
import com._1000meal.menu.dto.MenuGroupStockResponse;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.event.LowStock30Event;
import com._1000meal.menu.event.LowStockEvent;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceTest {

    @Mock
    MenuGroupRepository menuGroupRepository;

    @Mock
    MenuGroupStockRepository stockRepository;

    @Mock
    DailyMenuRepository dailyMenuRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    MenuGroupService service;

    @Test
    @DisplayName("재고 차감 성공 - 알림 조건 미충족 (31 초과에서 31 초과)")
    void deductStock_noNotification() {
        // given
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(50);
        when(stock.deduct(5)).thenReturn(new StockDeductResult(false, false));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        // when
        MenuGroupStockResponse response = service.deductStock(1L, DeductionUnit.MULTI_FIVE);

        // then
        verify(eventPublisher, never()).publishEvent(any());
        assertEquals(1L, response.getGroupId());
        assertEquals(50, response.getStock());
    }

    @Test
    @DisplayName("재고 차감 성공 - 10 임계치 알림 조건 충족 (11 → 10 이하)")
    void deductStock_withNotification10() {
        // given
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.deduct(5)).thenReturn(new StockDeductResult(false, true));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        // Store 모의 설정
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        WeeklyMenu weeklyMenu = mock(WeeklyMenu.class);
        when(weeklyMenu.getStore()).thenReturn(store);

        DailyMenu dailyMenu = mock(DailyMenu.class);
        when(dailyMenu.getWeeklyMenu()).thenReturn(weeklyMenu);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getDailyMenu()).thenReturn(dailyMenu);
        when(menuGroupRepository.findByIdWithStore(1L)).thenReturn(Optional.of(group));

        // 차감 후 재고값 설정
        when(stock.getStock()).thenReturn(8);

        // when
        service.deductStock(1L, DeductionUnit.MULTI_FIVE);

        // then
        ArgumentCaptor<LowStockEvent> eventCaptor = ArgumentCaptor.forClass(LowStockEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        LowStockEvent event = eventCaptor.getValue();
        assertEquals(100L, event.storeId());
        assertEquals("향설 1관", event.storeName());
        assertEquals(1L, event.groupId());
        assertEquals("기본 메뉴", event.groupName());
        assertEquals(8, event.remainingStock());
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void deductStock_insufficient() {
        // given
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.deduct(10)).thenThrow(new CustomException(MenuErrorCode.INSUFFICIENT_STOCK));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.deductStock(1L, DeductionUnit.MULTI_TEN));

        assertEquals(MenuErrorCode.INSUFFICIENT_STOCK, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("존재하지 않는 그룹 차감 시 예외 발생")
    void deductStock_groupNotFound() {
        // given
        when(stockRepository.findByMenuGroupIdForUpdate(999L)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.deductStock(999L, DeductionUnit.SINGLE));

        assertEquals(MenuErrorCode.MENU_GROUP_NOT_FOUND, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("중복 알림 방지 - 이미 알림 발송된 경우")
    void deductStock_duplicateNotificationPrevented() {
        // given: 이미 알림 발송된 상태에서 10 이하로 차감
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(5);
        when(stock.deduct(1)).thenReturn(new StockDeductResult(false, false));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        // when
        service.deductStock(1L, DeductionUnit.SINGLE);

        // then: 알림 이벤트 발행 안 됨
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("재고 직접 수정 성공")
    void updateStock_success() {
        // given
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(80);
        when(stockRepository.findByMenuGroupId(1L)).thenReturn(Optional.of(stock));

        // when
        MenuGroupStockResponse response = service.updateStock(1L, 80);

        // then
        verify(stock).updateStock(80);
        assertEquals(1L, response.getGroupId());
        assertEquals(80, response.getStock());
    }

    @Test
    @DisplayName("메뉴 그룹 조회 성공")
    void getMenuGroups_success() {
        // given
        Long storeId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 23);

        DailyMenu dailyMenu = mock(DailyMenu.class);
        when(dailyMenu.getId()).thenReturn(10L);
        when(dailyMenu.getDate()).thenReturn(date);
        when(dailyMenu.isOpen()).thenReturn(true);
        when(dailyMenu.isHoliday()).thenReturn(false);

        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date))
                .thenReturn(Optional.of(dailyMenu));

        // 그룹 모의 설정
        MenuGroupStock stock1 = mock(MenuGroupStock.class);
        when(stock1.getStock()).thenReturn(90);
        when(stock1.getCapacity()).thenReturn(100);

        MenuGroup group1 = mock(MenuGroup.class);
        when(group1.getId()).thenReturn(1L);
        when(group1.getName()).thenReturn("기본 메뉴");
        when(group1.getSortOrder()).thenReturn(0);
        when(group1.getStock()).thenReturn(stock1);
        when(group1.getMenus()).thenReturn(List.of());

        MenuGroupStock stock2 = mock(MenuGroupStock.class);
        when(stock2.getStock()).thenReturn(50);
        when(stock2.getCapacity()).thenReturn(50);

        MenuGroup group2 = mock(MenuGroup.class);
        when(group2.getId()).thenReturn(2L);
        when(group2.getName()).thenReturn("국밥 세트");
        when(group2.getSortOrder()).thenReturn(1);
        when(group2.getStock()).thenReturn(stock2);
        when(group2.getMenus()).thenReturn(List.of());

        when(menuGroupRepository.findByDailyMenuIdWithStockAndMenus(10L))
                .thenReturn(List.of(group1, group2));

        // when
        DailyMenuWithGroupsDto result = service.getMenuGroups(storeId, date);

        // then
        assertEquals(10L, result.getId());
        assertEquals(date, result.getDate());
        assertTrue(result.isOpen());
        assertEquals(140, result.getTotalStock());  // 90 + 50
        assertEquals(2, result.getGroups().size());
        assertEquals("기본 메뉴", result.getGroups().get(0).getName());
        assertEquals("국밥 세트", result.getGroups().get(1).getName());
    }

    @Test
    @DisplayName("그룹A 차감 시 그룹B stock은 변하지 않음")
    void deductStock_independentGroups() {
        // given: 그룹A 설정
        MenuGroupStock stockA = mock(MenuGroupStock.class);
        when(stockA.getStock()).thenReturn(85);
        when(stockA.deduct(5)).thenReturn(new StockDeductResult(false, false));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stockA));

        // when: 그룹A만 차감
        service.deductStock(1L, DeductionUnit.MULTI_FIVE);

        // then: 그룹A만 차감됨 (그룹B에 대한 stockRepository 조회는 발생하지 않음)
        verify(stockA).deduct(5);
        verify(stockRepository, times(1)).findByMenuGroupIdForUpdate(anyLong());
    }

    // ========== 30 임계치 관련 신규 테스트 ==========

    @Test
    @DisplayName("30 임계치 하향 돌파 - LowStock30Event 1회 발행")
    void deductStock_lowStock30Notification() {
        // given: before=31, after=30 → 30 임계치 돌파
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.deduct(1)).thenReturn(new StockDeductResult(true, false));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        WeeklyMenu weeklyMenu = mock(WeeklyMenu.class);
        when(weeklyMenu.getStore()).thenReturn(store);

        DailyMenu dailyMenu = mock(DailyMenu.class);
        when(dailyMenu.getWeeklyMenu()).thenReturn(weeklyMenu);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getDailyMenu()).thenReturn(dailyMenu);
        when(menuGroupRepository.findByIdWithStore(1L)).thenReturn(Optional.of(group));

        when(stock.getStock()).thenReturn(30);

        // when
        service.deductStock(1L, DeductionUnit.SINGLE);

        // then: LowStock30Event만 발행됨
        ArgumentCaptor<LowStock30Event> eventCaptor = ArgumentCaptor.forClass(LowStock30Event.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        LowStock30Event event = eventCaptor.getValue();
        assertEquals(100L, event.storeId());
        assertEquals("향설 1관", event.storeName());
        assertEquals(1L, event.groupId());
        assertEquals("기본 메뉴", event.groupName());
        assertEquals(30, event.remainingStock());
    }

    @Test
    @DisplayName("30 임계치 중복 방지 - 이미 30 알림 발송 후 추가 차감 시 이벤트 없음")
    void deductStock_lowStock30_noDuplicate() {
        // given: 이미 30 알림 발송된 상태 (29→28)
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(28);
        when(stock.deduct(1)).thenReturn(new StockDeductResult(false, false));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        // when
        service.deductStock(1L, DeductionUnit.SINGLE);

        // then: 이벤트 발행 없음
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("31 → 5 대량 차감 시 30 + 10 두 이벤트 모두 발행")
    void deductStock_bothThresholdsCrossed() {
        // given: 31에서 5로 대량 차감 → 30 및 10 동시 돌파
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.deduct(10)).thenReturn(new StockDeductResult(true, true));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        WeeklyMenu weeklyMenu = mock(WeeklyMenu.class);
        when(weeklyMenu.getStore()).thenReturn(store);

        DailyMenu dailyMenu = mock(DailyMenu.class);
        when(dailyMenu.getWeeklyMenu()).thenReturn(weeklyMenu);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getDailyMenu()).thenReturn(dailyMenu);
        when(menuGroupRepository.findByIdWithStore(1L)).thenReturn(Optional.of(group));

        when(stock.getStock()).thenReturn(5);

        // when
        service.deductStock(1L, DeductionUnit.MULTI_TEN);

        // then: 두 이벤트 모두 발행
        ArgumentCaptor<LowStock30Event> event30Captor = ArgumentCaptor.forClass(LowStock30Event.class);
        verify(eventPublisher).publishEvent(event30Captor.capture());

        LowStock30Event event30 = event30Captor.getValue();
        assertEquals(100L, event30.storeId());
        assertEquals("향설 1관", event30.storeName());
        assertEquals(5, event30.remainingStock());

        ArgumentCaptor<LowStockEvent> event10Captor = ArgumentCaptor.forClass(LowStockEvent.class);
        verify(eventPublisher).publishEvent(event10Captor.capture());

        LowStockEvent event10 = event10Captor.getValue();
        assertEquals(100L, event10.storeId());
        assertEquals("향설 1관", event10.storeName());
        assertEquals(5, event10.remainingStock());
    }
}
