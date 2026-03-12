package com._1000meal.menu.service;

import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuGroupStockResetServiceTest {

    @Mock
    MenuGroupStockRepository menuGroupStockRepository;

    @InjectMocks
    MenuGroupStockResetService menuGroupStockResetService;

    @Test
    @DisplayName("capacity가 80이고 stock이 0이면 80으로 초기화된다")
    void resetAllStocksToCapacity_resetsToEighty() throws Exception {
        MenuGroupStock stock = stock(1L, 80, 0);
        when(menuGroupStockRepository.findAll()).thenReturn(List.of(stock));

        MenuGroupStockResetService.StockResetSummary summary = menuGroupStockResetService.resetAllStocksToCapacity();

        assertEquals(80, stock.getStock());
        assertEquals(1, summary.getTotalCount());
        assertEquals(1, summary.getResetCount());
        assertEquals(0, summary.getSkipCount());
        assertEquals(0, summary.getExceptionCount());
        verify(menuGroupStockRepository).findAll();
        verifyNoMoreInteractions(menuGroupStockRepository);
    }

    @Test
    @DisplayName("capacity가 40이고 stock이 13이면 40으로 초기화된다")
    void resetAllStocksToCapacity_resetsToForty() throws Exception {
        MenuGroupStock stock = stock(2L, 40, 13);
        when(menuGroupStockRepository.findAll()).thenReturn(List.of(stock));

        MenuGroupStockResetService.StockResetSummary summary = menuGroupStockResetService.resetAllStocksToCapacity();

        assertEquals(40, stock.getStock());
        assertEquals(1, summary.getResetCount());
        assertEquals(0, summary.getSkipCount());
        assertEquals(0, summary.getExceptionCount());
    }

    @Test
    @DisplayName("capacity가 0이면 skip 처리된다")
    void resetAllStocksToCapacity_skipsInvalidCapacity() throws Exception {
        MenuGroupStock stock = stock(3L, 0, 25);
        when(menuGroupStockRepository.findAll()).thenReturn(List.of(stock));

        MenuGroupStockResetService.StockResetSummary summary = menuGroupStockResetService.resetAllStocksToCapacity();

        assertEquals(25, stock.getStock());
        assertEquals(1, summary.getTotalCount());
        assertEquals(0, summary.getResetCount());
        assertEquals(1, summary.getSkipCount());
        assertEquals(0, summary.getExceptionCount());
        assertTrue(summary.getExceptionSummaries().isEmpty());
    }

    @Test
    @DisplayName("그룹이 2개면 각각 개별 초기화된다")
    void resetAllStocksToCapacity_resetsEachGroupIndependently() throws Exception {
        MenuGroupStock stock1 = stock(4L, 40, 13);
        MenuGroupStock stock2 = stock(5L, 40, 0);
        when(menuGroupStockRepository.findAll()).thenReturn(List.of(stock1, stock2));

        MenuGroupStockResetService.StockResetSummary summary = menuGroupStockResetService.resetAllStocksToCapacity();

        assertEquals(40, stock1.getStock());
        assertEquals(40, stock2.getStock());
        assertEquals(2, summary.getTotalCount());
        assertEquals(2, summary.getResetCount());
        assertEquals(0, summary.getSkipCount());
        assertEquals(0, summary.getExceptionCount());
    }

    @Test
    @DisplayName("기존 수동 수정 후에도 일일 초기화는 capacity 기준으로 동작한다")
    void resetAllStocksToCapacity_afterManualUpdateUsesCapacity() throws Exception {
        MenuGroupStock stock = stock(6L, 80, 20);
        stock.updateStock(7);
        when(menuGroupStockRepository.findAll()).thenReturn(List.of(stock));

        MenuGroupStockResetService.StockResetSummary summary = menuGroupStockResetService.resetAllStocksToCapacity();

        assertEquals(80, stock.getStock());
        assertEquals(1, summary.getResetCount());
        assertEquals(0, summary.getSkipCount());
    }

    private MenuGroupStock stock(Long groupId, int capacity, int currentStock) throws Exception {
        MenuGroup group = new MenuGroup(null, null, "group-" + groupId, 0, false);
        setField(group, "id", groupId);

        MenuGroupStock stock = MenuGroupStock.of(group, capacity);
        setField(stock, "stock", currentStock);
        return stock;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
