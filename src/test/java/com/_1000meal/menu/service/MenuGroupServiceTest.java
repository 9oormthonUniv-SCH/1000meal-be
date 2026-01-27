package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.*;
import com._1000meal.menu.dto.DailyMenuWithGroupsDto;
import com._1000meal.menu.dto.GroupDailyMenuResponse;
import com._1000meal.menu.dto.MenuGroupStockResponse;
import com._1000meal.menu.dto.MenuUpdateRequest;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.event.LowStock30Event;
import com._1000meal.menu.event.LowStockEvent;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.ZoneId;
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
    GroupDailyMenuRepository groupDailyMenuRepository;

    @Mock
    StoreRepository storeRepository;

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
        when(stock.deduct(eq(5), any(LocalDate.class))).thenReturn(new StockDeductResult(false, false));
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
        when(stock.deduct(eq(5), any(LocalDate.class))).thenReturn(new StockDeductResult(false, true));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        // Store 모의 설정
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStore()).thenReturn(store);
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
        when(stock.deduct(eq(10), any(LocalDate.class))).thenThrow(new CustomException(MenuErrorCode.INSUFFICIENT_STOCK));
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
        when(stock.deduct(eq(1), any(LocalDate.class))).thenReturn(new StockDeductResult(false, false));
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

        // 그룹 모의 설정
        MenuGroupStock stock1 = mock(MenuGroupStock.class);
        when(stock1.getStock()).thenReturn(90);
        when(stock1.getCapacity()).thenReturn(100);

        MenuGroup group1 = mock(MenuGroup.class);
        when(group1.getId()).thenReturn(1L);
        when(group1.getName()).thenReturn("기본 메뉴");
        when(group1.getSortOrder()).thenReturn(0);
        when(group1.getStock()).thenReturn(stock1);

        MenuGroupStock stock2 = mock(MenuGroupStock.class);
        when(stock2.getStock()).thenReturn(50);
        when(stock2.getCapacity()).thenReturn(50);

        MenuGroup group2 = mock(MenuGroup.class);
        when(group2.getId()).thenReturn(2L);
        when(group2.getName()).thenReturn("국밥 세트");
        when(group2.getSortOrder()).thenReturn(1);
        when(group2.getStock()).thenReturn(stock2);

        when(menuGroupRepository.findByStoreIdWithStock(storeId))
                .thenReturn(List.of(group1, group2));
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDate(List.of(1L, 2L), date))
                .thenReturn(List.of());
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date))
                .thenReturn(Optional.empty());

        // when
        DailyMenuWithGroupsDto result = service.getMenuGroups(storeId, date);

        // then
        assertEquals(date, result.getDate());
        assertTrue(result.isOpen());
        assertFalse(result.isHoliday());
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
        when(stockA.deduct(eq(5), any(LocalDate.class))).thenReturn(new StockDeductResult(false, false));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stockA));

        // when: 그룹A만 차감
        service.deductStock(1L, DeductionUnit.MULTI_FIVE);

        // then: 그룹A만 차감됨 (그룹B에 대한 stockRepository 조회는 발생하지 않음)
        verify(stockA).deduct(eq(5), any(LocalDate.class));
        verify(stockRepository, times(1)).findByMenuGroupIdForUpdate(anyLong());
    }

    // ========== 30 임계치 관련 신규 테스트 ==========

    @Test
    @DisplayName("30 임계치 하향 돌파 - LowStock30Event 1회 발행")
    void deductStock_lowStock30Notification() {
        // given: before=31, after=30 → 30 임계치 돌파
        MenuGroupStock stock = MenuGroupStock.of(mock(MenuGroup.class), 31);
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(1L)).thenReturn(Optional.of(group));

        // when
        LocalDate fixedToday = LocalDate.of(2026, 1, 23);
        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixedToday);
            service.deductStock(1L, DeductionUnit.SINGLE);
        }

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
        // given: 같은 날 31→30 알림 발송 후 추가 차감
        MenuGroupStock stock = MenuGroupStock.of(mock(MenuGroup.class), 31);
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(1L)).thenReturn(Optional.of(group));

        LocalDate fixedToday = LocalDate.of(2026, 1, 23);
        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixedToday);
            // when
            service.deductStock(1L, DeductionUnit.SINGLE); // 31 -> 30 (notify)
            service.deductStock(1L, DeductionUnit.SINGLE); // 30 -> 29 (no notify)
        }

        // then: 이벤트 발행 없음
        verify(eventPublisher, times(1)).publishEvent(any(LowStock30Event.class));
    }

    @Test
    @DisplayName("30 임계치 - 전날 알림이면 오늘 다시 발송")
    void deductStock_lowStock30_notifiedYesterday_triggersToday() throws Exception {
        MenuGroupStock stock = MenuGroupStock.of(mock(MenuGroup.class), 30);
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(1L)).thenReturn(Optional.of(group));

        LocalDate fixedToday = LocalDate.of(2026, 1, 23);
        setField(stock, "lastNotifiedDate", fixedToday.minusDays(1));
        setField(stock, "lastNotifiedThreshold", 30);

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixedToday);
            service.deductStock(1L, DeductionUnit.SINGLE); // 30 -> 29 (notify again)
        }

        verify(eventPublisher, times(1)).publishEvent(any(LowStock30Event.class));
    }

    @Test
    @DisplayName("31 → 5 대량 차감 시 30 + 10 두 이벤트 모두 발행")
    void deductStock_bothThresholdsCrossed() {
        // given: 31에서 5로 대량 차감 → 30 및 10 동시 돌파
        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.deduct(eq(10), any(LocalDate.class))).thenReturn(new StockDeductResult(true, true));
        when(stockRepository.findByMenuGroupIdForUpdate(1L)).thenReturn(Optional.of(stock));

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(100L);
        when(store.getName()).thenReturn("향설 1관");

        MenuGroup group = mock(MenuGroup.class);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStore()).thenReturn(store);
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

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ========== 메뉴 등록/교체 테스트 (GroupDailyMenu 기반) ==========

    @Test
    @DisplayName("updateMenusInGroup: 성공 - 새로운 (groupId, date) → GroupDailyMenu 생성")
    void updateMenusInGroup_success_creates() {
        // given
        Long groupId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 23);

        MenuGroup menuGroup = mock(MenuGroup.class);
        when(menuGroup.getId()).thenReturn(groupId);
        when(menuGroup.getName()).thenReturn("향설 1관");
        when(menuGroupRepository.findById(groupId)).thenReturn(Optional.of(menuGroup));

        // (groupId, date)에 대한 기존 데이터 없음
        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, date))
                .thenReturn(Optional.empty());

        when(groupDailyMenuRepository.save(any(GroupDailyMenu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MenuUpdateRequest request = new MenuUpdateRequest(List.of("떡볶이", "순대", "튀김"));

        // when
        GroupDailyMenuResponse result = service.updateMenusInGroup(groupId, date, request);

        // then
        verify(groupDailyMenuRepository).save(any(GroupDailyMenu.class));
        assertNotNull(result);
        assertEquals(groupId, result.getGroupId());
        assertEquals("향설 1관", result.getGroupName());
        assertEquals(date, result.getDate());
        assertEquals(List.of("떡볶이", "순대", "튀김"), result.getMenus());
    }

    @Test
    @DisplayName("updateMenusInGroup: 성공 - 기존 (groupId, date) → 메뉴 교체")
    void updateMenusInGroup_success_replaces() {
        // given
        Long groupId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 23);

        MenuGroup menuGroup = mock(MenuGroup.class);
        when(menuGroup.getId()).thenReturn(groupId);
        when(menuGroup.getName()).thenReturn("향설 1관");
        when(menuGroupRepository.findById(groupId)).thenReturn(Optional.of(menuGroup));

        // 기존 GroupDailyMenu 존재
        GroupDailyMenu existing = mock(GroupDailyMenu.class);
        when(existing.getMenuGroup()).thenReturn(menuGroup);
        when(existing.getDate()).thenReturn(date);
        when(existing.getMenuNames()).thenReturn(List.of("떡볶이", "순대", "튀김"));

        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, date))
                .thenReturn(Optional.of(existing));

        when(groupDailyMenuRepository.save(any(GroupDailyMenu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MenuUpdateRequest request = new MenuUpdateRequest(List.of("라면", "김밥"));

        // when
        service.updateMenusInGroup(groupId, date, request);

        // then
        verify(existing).replaceMenus(List.of("라면", "김밥"));
        verify(groupDailyMenuRepository).save(existing);
    }

    @Test
    @DisplayName("updateMenusInGroup: 그룹 미존재 시 MENU_GROUP_NOT_FOUND")
    void updateMenusInGroup_groupNotFound() {
        // given
        LocalDate date = LocalDate.of(2026, 1, 23);
        when(menuGroupRepository.findById(999L)).thenReturn(Optional.empty());

        MenuUpdateRequest request = new MenuUpdateRequest(List.of("떡볶이"));

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.updateMenusInGroup(999L, date, request));

        assertEquals(MenuErrorCode.MENU_GROUP_NOT_FOUND, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("updateMenusInGroup: 정제 후 빈 메뉴 시 MENU_EMPTY")
    void updateMenusInGroup_emptyAfterCleaning() {
        // given
        LocalDate date = LocalDate.of(2026, 1, 23);

        MenuGroup menuGroup = mock(MenuGroup.class);
        when(menuGroupRepository.findById(1L)).thenReturn(Optional.of(menuGroup));

        // 빈 문자열과 공백만 있는 리스트
        MenuUpdateRequest request = new MenuUpdateRequest(List.of("  ", ""));

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.updateMenusInGroup(1L, date, request));

        assertEquals(MenuErrorCode.MENU_EMPTY, ex.getErrorCodeIfs());
    }

    // ========== 기본 그룹 삭제 방어 테스트 ==========

    @Test
    @DisplayName("deleteMenuGroup: 기본 그룹 삭제 시 CANNOT_DELETE_DEFAULT_GROUP")
    void deleteMenuGroup_defaultGroupBlocked() {
        // given
        MenuGroup menuGroup = mock(MenuGroup.class);
        when(menuGroup.isDefault()).thenReturn(true);
        when(menuGroupRepository.findById(1L)).thenReturn(Optional.of(menuGroup));

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.deleteMenuGroup(1L));

        assertEquals(MenuErrorCode.CANNOT_DELETE_DEFAULT_GROUP, ex.getErrorCodeIfs());
        verify(menuGroupRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteMenuGroup: 일반 그룹은 정상 삭제")
    void deleteMenuGroup_normalGroupDeleted() {
        // given
        MenuGroup menuGroup = mock(MenuGroup.class);
        when(menuGroup.isDefault()).thenReturn(false);
        when(menuGroupRepository.findById(2L)).thenReturn(Optional.of(menuGroup));

        // when
        service.deleteMenuGroup(2L);

        // then
        verify(menuGroupRepository).delete(menuGroup);
    }

    // ========== 그룹 생성 시 메뉴 없이 생성되는지 테스트 ==========

    @Test
    @DisplayName("createMenuGroup: store 기준으로 그룹 생성 성공")
    void createMenuGroup_success() {
        // given
        Long storeId = 1L;

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        com._1000meal.menu.dto.MenuGroupCreateRequest request =
                com._1000meal.menu.dto.MenuGroupCreateRequest.builder()
                        .name("크앙분식")
                        .sortOrder(1)
                        .capacity(50)
                        .build();

        ArgumentCaptor<MenuGroup> captor = ArgumentCaptor.forClass(MenuGroup.class);
        when(menuGroupRepository.save(any(MenuGroup.class))).thenAnswer(invocation -> {
            MenuGroup saved = invocation.getArgument(0);
            return saved;
        });

        // when
        var result = service.createMenuGroup(storeId, request);

        // then
        assertNotNull(result);
        assertEquals("크앙분식", result.getName());
        assertEquals(1, result.getSortOrder());
        assertEquals(50, result.getCapacity());
        // 메뉴가 생성되지 않았음을 확인 (비어있어야 함)
        assertTrue(result.getMenus().isEmpty());
        verify(menuGroupRepository).save(captor.capture());
        assertFalse(captor.getValue().isDefault());
        assertEquals(store, captor.getValue().getStore());
        verifyNoInteractions(dailyMenuRepository, groupDailyMenuRepository);
    }

    @Test
    @DisplayName("createMenuGroup: 이름이 비어 있으면 INVALID_MENU_NAME")
    void createMenuGroup_nameRequired() {
        // given
        Long storeId = 1L;
        com._1000meal.menu.dto.MenuGroupCreateRequest request =
                com._1000meal.menu.dto.MenuGroupCreateRequest.builder()
                        .name("   ")
                        .build();

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.createMenuGroup(storeId, request));

        assertEquals(MenuErrorCode.INVALID_MENU_NAME, ex.getErrorCodeIfs());
        verifyNoInteractions(storeRepository, menuGroupRepository);
    }

    @Test
    @DisplayName("createMenuGroup: DailyMenu 없이도 정상 생성")
    void createMenuGroup_withoutDailyMenu() {
        // given
        Long storeId = 1L;

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        com._1000meal.menu.dto.MenuGroupCreateRequest request =
                com._1000meal.menu.dto.MenuGroupCreateRequest.builder()
                        .name("신규 그룹")
                        .build();

        when(menuGroupRepository.save(any(MenuGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var result = service.createMenuGroup(storeId, request);

        // then
        assertNotNull(result);
        assertEquals("신규 그룹", result.getName());
        verifyNoInteractions(dailyMenuRepository, groupDailyMenuRepository);
    }

    @Test
    @DisplayName("getMenuGroups: DailyMenu 없으면 기본 open/holiday, 그룹 스켈레톤 반환")
    void getMenuGroups_withoutDailyMenu_returnsSkeleton() {
        // given
        Long storeId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 23);

        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(70);
        when(stock.getCapacity()).thenReturn(100);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(1L);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getSortOrder()).thenReturn(0);
        when(group.getStock()).thenReturn(stock);

        when(menuGroupRepository.findByStoreIdWithStock(storeId))
                .thenReturn(List.of(group));
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDate(List.of(1L), date))
                .thenReturn(List.of());
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date))
                .thenReturn(Optional.empty());

        // when
        DailyMenuWithGroupsDto result = service.getMenuGroups(storeId, date);

        // then
        assertNull(result.getId());
        assertEquals(date, result.getDate());
        assertEquals(date.getDayOfWeek(), result.getDayOfWeek());
        assertTrue(result.isOpen());
        assertFalse(result.isHoliday());
        assertEquals(70, result.getTotalStock());
        assertEquals(1, result.getGroups().size());
        assertTrue(result.getGroups().get(0).getMenus().isEmpty());
    }

    @Test
    @DisplayName("getMenuGroups: 일부 그룹에 GroupDailyMenu 있으면 해당 그룹만 메뉴 채움")
    void getMenuGroups_groupDailyMenuApplied() {
        // given
        Long storeId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 23);

        MenuGroupStock stock1 = mock(MenuGroupStock.class);
        when(stock1.getStock()).thenReturn(30);
        when(stock1.getCapacity()).thenReturn(50);

        MenuGroup group1 = mock(MenuGroup.class);
        when(group1.getId()).thenReturn(1L);
        when(group1.getName()).thenReturn("그룹1");
        when(group1.getSortOrder()).thenReturn(0);
        when(group1.getStock()).thenReturn(stock1);

        MenuGroupStock stock2 = mock(MenuGroupStock.class);
        when(stock2.getStock()).thenReturn(20);
        when(stock2.getCapacity()).thenReturn(20);

        MenuGroup group2 = mock(MenuGroup.class);
        when(group2.getId()).thenReturn(2L);
        when(group2.getName()).thenReturn("그룹2");
        when(group2.getSortOrder()).thenReturn(1);
        when(group2.getStock()).thenReturn(stock2);

        when(menuGroupRepository.findByStoreIdWithStock(storeId))
                .thenReturn(List.of(group1, group2));

        GroupDailyMenu groupDailyMenu = mock(GroupDailyMenu.class);
        when(groupDailyMenu.getMenuGroup()).thenReturn(group1);
        when(groupDailyMenu.getMenuNames()).thenReturn(List.of("떡볶이", "순대"));

        when(groupDailyMenuRepository.findByMenuGroupIdInAndDate(List.of(1L, 2L), date))
                .thenReturn(List.of(groupDailyMenu));
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date))
                .thenReturn(Optional.empty());

        // when
        DailyMenuWithGroupsDto result = service.getMenuGroups(storeId, date);

        // then
        assertEquals(2, result.getGroups().size());
        assertEquals(List.of("떡볶이", "순대"), result.getGroups().get(0).getMenus());
        assertTrue(result.getGroups().get(1).getMenus().isEmpty());
        assertEquals(50, result.getTotalStock());
    }
}
