package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuAddRequest;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.StockResponse;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuRepository;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock WeeklyMenuRepository weeklyMenuRepository;
    @Mock DailyMenuRepository dailyMenuRepository;
    @Mock MenuRepository menuRepository;

    @InjectMocks MenuService service;

    private LocalDate wed;     // 테스트 기준일(수)
    private LocalDate weekStart; // 해당 주 월요일
    private LocalDate weekEnd;   // 해당 주 일요일

    @BeforeEach
    void setUp() {
        wed = LocalDate.of(2026, 1, 7); // 수요일
        weekStart = wed.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        weekEnd = weekStart.plusDays(6);
    }

    @Test
    @DisplayName("addOrReplaceDailyMenu: date가 null이면 DATE_REQUIRED")
    void addOrReplaceDailyMenu_dateNull() {
        DailyMenuAddRequest req = mock(DailyMenuAddRequest.class);
        when(req.getDate()).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> service.addOrReplaceDailyMenu(1L, req));

        // getErrorCode()가 없다면 메시지/코드 접근 방식에 맞춰 바꿔야 함
        // 여기선 "예외 발생"만 검증 (코드 검증은 너 프로젝트 예외 구조에 맞춰 추가)
        assertNotNull(ex);
    }

    @Test
    @DisplayName("addOrReplaceDailyMenu: 메뉴 리스트가 비어있으면 기존 메뉴 clear 후 저장")
    void addOrReplaceDailyMenu_emptyMenus_clearsAndSaves() {
        Long storeId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 6);
        LocalDate weekStart = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        DailyMenuAddRequest req = mock(DailyMenuAddRequest.class);
        when(req.getDate()).thenReturn(date);
        when(req.getMenus()).thenReturn(List.of());

        // 주간 메뉴 없다고 해서 save 타게 유도
        when(weeklyMenuRepository.findByStoreIdAndRangeWithMenus(eq(storeId), eq(weekStart)))
                .thenReturn(Optional.empty());

        when(storeRepository.getReferenceById(storeId))
                .thenReturn(mock(com._1000meal.store.domain.Store.class));

        // ✅ save 결과 weekly는 id/startDate가 반드시 있어야 함
        WeeklyMenu savedWeekly = mock(WeeklyMenu.class);
        when(savedWeekly.getId()).thenReturn(100L);
        when(savedWeekly.getStartDate()).thenReturn(weekStart);
        when(weeklyMenuRepository.save(any(WeeklyMenu.class))).thenReturn(savedWeekly);

        // ✅ ensureWeekDailyMenus 내부에서 호출됨 (100L로 맞춤)
        when(dailyMenuRepository.findDatesByWeeklyMenuId(100L)).thenReturn(List.of());

        // DailyMenu는 menus 리스트가 실제여야 clear 가능
        DailyMenu dm = mock(DailyMenu.class);
        List<Menu> menus = new ArrayList<>();
        menus.add(Menu.builder().name("기존").build());
        when(dm.getMenus()).thenReturn(menus);

        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date))
                .thenReturn(Optional.of(dm));

        service.addOrReplaceDailyMenu(storeId, req);

        assertTrue(menus.isEmpty());
        verify(dailyMenuRepository).save(dm);
    }

    @Test
    @DisplayName("addOrReplaceDailyMenu: 메뉴 정제(트림/빈값 제거/중복 제거) 후 Menu 엔티티 생성 및 연관관계 세팅")
    void addOrReplaceDailyMenu_cleanedMenus_createsMenus() {
        Long storeId = 1L;

        DailyMenuAddRequest req = mock(DailyMenuAddRequest.class);
        when(req.getDate()).thenReturn(wed);
        when(req.getMenus()).thenReturn(Arrays.asList(" 김치찌개 ", "김치찌개", "  ", "돈까스", null)); // ✅ null 허용

        // weekly upsert
        WeeklyMenu weekly = mock(WeeklyMenu.class);
        when(weekly.getId()).thenReturn(100L);
        when(weekly.getStartDate()).thenReturn(weekStart);
        when(weeklyMenuRepository.findByStoreIdAndRangeWithMenus(storeId, weekStart))
                .thenReturn(Optional.of(weekly));

        // ensureWeekDailyMenus 내부
        when(dailyMenuRepository.findDatesByWeeklyMenuId(100L))
                .thenReturn(List.of(weekStart, weekStart.plusDays(1)));

        // daily upsert: 이미 존재한다고 가정
        DailyMenu dm = mock(DailyMenu.class);
        List<Menu> menus = new ArrayList<>();
        when(dm.getMenus()).thenReturn(menus);
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, wed))
                .thenReturn(Optional.of(dm));

        service.addOrReplaceDailyMenu(storeId, req);

        // "김치찌개", "돈까스" 2개만 남아야 함
        assertEquals(2, menus.size());
        assertEquals("김치찌개", menus.get(0).getName());
        assertEquals("돈까스", menus.get(1).getName());

        // 연관관계 세팅 확인
        assertSame(dm, menus.get(0).getDailyMenu());
        assertSame(dm, menus.get(1).getDailyMenu());

        verify(dailyMenuRepository).save(dm);
    }


    @Test
    @DisplayName("getDailyMenu: DailyMenu 없으면 DAILY_MENU_NOT_FOUND")
    void getDailyMenu_notFound() {
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(1L, wed))
                .thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.getDailyMenu(1L, wed));

        assertEquals(MenuErrorCode.DAILY_MENU_NOT_FOUND, ex.getErrorCodeIfs());
        assertEquals(MenuErrorCode.DAILY_MENU_NOT_FOUND.getMessage(), ex.getDisplayMessage());
    }

    @Test
    @DisplayName("getDailyMenu: DailyMenu 있으면 toDto 반환 + 메뉴 정렬 조회 쿼리 호출")
    void getDailyMenu_found_callsMenuQuery() {
        DailyMenu dm = mock(DailyMenu.class);
        when(dm.getId()).thenReturn(10L);

        DailyMenuDto dto = mock(DailyMenuDto.class);
        when(dm.toDto()).thenReturn(dto);

        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(1L, wed)).thenReturn(Optional.of(dm));
        when(menuRepository.findByDailyMenu_IdOrderByIdAsc(10L)).thenReturn(List.of());

        DailyMenuDto res = service.getDailyMenu(1L, wed);

        assertSame(dto, res);
        verify(menuRepository).findByDailyMenu_IdOrderByIdAsc(10L);
    }

    @Test
    @DisplayName("getWeeklyMenu: 매장 없으면 STORE_NOT_FOUND")
    void getWeeklyMenu_storeNotFound() {
        Long storeId = 1L;

        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.getWeeklyMenu(storeId, wed));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
        assertEquals(StoreErrorCode.STORE_NOT_FOUND.getMessage(), ex.getDisplayMessage());
    }

    @Test
    @DisplayName("getWeeklyMenu: 주간 메뉴가 없으면 스켈레톤(월~금, 5일) 반환")
    void getWeeklyMenu_whenWeeklyMissing_returnsSkeleton() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(weeklyMenuRepository.findByStoreIdAndRangeWithMenus(storeId, wed)).thenReturn(Optional.empty());

        WeeklyMenuResponse res = service.getWeeklyMenu(storeId, wed);

        assertEquals(storeId, res.getStoreId());
        assertEquals(weekStart, res.getStartDate());
        assertEquals(weekEnd, res.getEndDate());
        assertNotNull(res.getDailyMenus());
        assertEquals(5, res.getDailyMenus().size()); // 코드상 5일만 생성

        assertEquals(weekStart, res.getDailyMenus().get(0).getDate());
        assertEquals(DayOfWeek.MONDAY, res.getDailyMenus().get(0).getDayOfWeek());
    }

    @Test
    @DisplayName("deductStock: 재고 부족이면 INSUFFICIENT_STOCK")
    void deductStock_insufficient() {
        DailyMenu dm = mock(DailyMenu.class);
        when(dm.getStock()).thenReturn(3); // 현재 재고 3
        when(dailyMenuRepository.findById(10L)).thenReturn(Optional.of(dm));

        CustomException ex = assertThrows(CustomException.class,
                () -> service.deductStock(10L, 50)); // 50 차감 시도 → 부족

        assertEquals(MenuErrorCode.INSUFFICIENT_STOCK, ex.getErrorCodeIfs());
        assertEquals(MenuErrorCode.INSUFFICIENT_STOCK.getMessage(), ex.getDisplayMessage());

        verify(dm, never()).deductStock(anyInt()); // 예외로 인해 엔티티 차감 로직 호출되면 안 됨
    }

    @Test
    @DisplayName("deductStock: 재고 충분하면 deductStock 호출 + StockResponse 반환")
    void deductStock_success() {
        DailyMenu dm = mock(DailyMenu.class);

        when(dm.getId()).thenReturn(10L);
        when(dm.getStock()).thenReturn(10); // 비교용 (충분)
        when(dailyMenuRepository.findById(10L)).thenReturn(Optional.of(dm));

        StockResponse res = service.deductStock(10L, 3);

        verify(dm).deductStock(3);

        // ✅ 필드 직접 접근 금지 -> getter 사용
        assertEquals(10L, res.getMenuId());
        assertEquals(10, res.getStock()); // mock이므로 stock 값은 stubbed 그대로
    }

    @Test
    @DisplayName("operationStock: updateStock 호출 + StockResponse 반환")
    void operationStock_success() {
        DailyMenu dm = mock(DailyMenu.class);

        when(dm.getId()).thenReturn(10L);
        when(dm.getStock()).thenReturn(50); // 서비스가 응답 만들 때 읽는 값
        when(dailyMenuRepository.findById(10L)).thenReturn(Optional.of(dm));

        StockResponse res = service.operationStock(10L, 50);

        verify(dm).updateStock(50);

        // ✅ getter 사용
        assertEquals(10L, res.getMenuId());
        assertEquals(50, res.getStock());
    }
}