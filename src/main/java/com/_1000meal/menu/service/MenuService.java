package com._1000meal.menu.service;

import com._1000meal.favoriteMenu.repository.FavoriteMenuRepository;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.DailyMenu;
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
import com._1000meal.global.error.code.MenuErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {


    private final StoreRepository storeRepository;
    private final WeeklyMenuRepository weeklyMenuRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final MenuRepository menuRepository;
    private final FavoriteMenuRepository favoriteMenuRepository;

    @Transactional
    public void addOrReplaceDailyMenu(Long storeId, DailyMenuAddRequest req) {

        LocalDate targetDate = req.getDate();
        if (targetDate == null) throw new CustomException(MenuErrorCode.DATE_REQUIRED);

        List<String> raw = Optional.ofNullable(req.getMenus()).orElse(List.of());
        List<String> cleaned = raw.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        WeeklyMenu weekly = upsertWeeklyMenu(storeId, targetDate);
        DailyMenu dm = upsertDailyMenu(storeId, targetDate, weekly);

        if (cleaned.isEmpty()) {
            dm.getMenus().clear();
            dailyMenuRepository.save(dm);
            return;
        }

        dm.getMenus().clear();
        for (String name : cleaned) {
            Menu m = Menu.builder().name(name).build();
            m.setDailyMenu(dm);
            dm.getMenus().add(m);
        }
        dailyMenuRepository.save(dm);
    }

    private WeeklyMenu upsertWeeklyMenu(Long storeId, LocalDate anyDateInWeek) {
        LocalDate weekStart = anyDateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd   = weekStart.plusDays(6);

        WeeklyMenu weekly = weeklyMenuRepository.findByStoreIdAndRangeWithMenus(storeId, weekStart)
                .orElseGet(() -> {
                    WeeklyMenu wm = WeeklyMenu.builder()
                            .store(storeRepository.getReferenceById(storeId))
                            .startDate(weekStart)
                            .endDate(weekEnd)
                            .build();
                    return weeklyMenuRepository.save(wm);
                });

        // ★ 주간 스캐폴딩: 부족한 DailyMenu 자동 생성
        ensureWeekDailyMenus(weekly);
        return weekly;
    }

    private DailyMenu upsertDailyMenu(Long storeId, LocalDate date, WeeklyMenu weekly) {
        return dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElseGet(() -> {
                    DailyMenu dm = DailyMenu.builder()
                            .weeklyMenu(weekly)
                            .date(date)
                            .build();
                        return dailyMenuRepository.save(dm);
                });
    }

    @Transactional(readOnly = true)
    public DailyMenuDto getDailyMenu(Long storeId, LocalDate date) {

        DailyMenu dm = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElseThrow(() -> new CustomException(MenuErrorCode.DAILY_MENU_NOT_FOUND));

        // 안정된 순서로 메뉴 반환 (id 오름차순)
        List<Menu> menus = menuRepository.findByDailyMenu_IdOrderByIdAsc(dm.getId());
        return dm.toDto();
    }

    @Transactional(readOnly = true)
    public WeeklyMenuResponse getWeeklyMenu(Long storeId, LocalDate date) {
        // 1. 매장 존재 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        // 2. 오늘 날짜 기준
        //LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 3. 주간 메뉴 가져 오기
        WeeklyMenu weeklyMenu = weeklyMenuRepository
                .findByStoreIdAndRangeWithMenus(storeId, date)
                .orElseThrow(() -> new CustomException(MenuErrorCode.WEEKLY_MENU_NOT_FOUND));

        // 4. DailyMenu 매핑
        List<DailyMenuDto> dailyDtos = weeklyMenu.getDailyMenus().stream()
                .map(DailyMenu::toDto)
                .collect(Collectors.toList());

        // 5. 최종 응답
        return WeeklyMenuResponse.builder()
                .storeId(store.getId())
                .startDate(weeklyMenu.getStartDate())
                .endDate(weeklyMenu.getEndDate())
                .dailyMenus(dailyDtos)
                .build();
    }


    @Transactional
    public StockResponse deductStock(Long menuId, Integer value) {
        DailyMenu dailyMenu = dailyMenuRepository.findById(menuId) // Lock?
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        if(dailyMenu.getStock() < value) {
            throw new CustomException(MenuErrorCode.INSUFFICIENT_STOCK);
        }

        dailyMenu.deductStock(value);
        return new StockResponse(dailyMenu.getId(), dailyMenu.getStock());
    }

    @Transactional
    public StockResponse operationStock(Long menuId, Integer stock) {
        DailyMenu dailyMenu = dailyMenuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        dailyMenu.updateStock(stock);
        return new StockResponse(dailyMenu.getId(), dailyMenu.getStock());
    }

    @Transactional
    protected void ensureWeekDailyMenus(WeeklyMenu weekly) {
        LocalDate start = weekly.getStartDate(); // 반드시 주의 시작(월요일)이어야 함

        // 이미 있는 날짜 수집
        Set<LocalDate> existing = new HashSet<>(
                dailyMenuRepository.findDatesByWeeklyMenuId(weekly.getId())
        );

        // 월~금(5일)만 생성
        List<DailyMenu> toCreate = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) { // 0=월, 4=금
            LocalDate d = start.plusDays(i);
            if (!existing.contains(d)) {
                DailyMenu dm = DailyMenu.builder()
                        .weeklyMenu(weekly)
                        .date(d)
                        .build();
                // DailyMenu에 store가 NotNull이면 아래 주석 해제
                // dm.setStore(weekly.getStore());
                toCreate.add(dm);
            }
        }

        if (!toCreate.isEmpty()) {
            dailyMenuRepository.saveAll(toCreate);
        }
    }
}
