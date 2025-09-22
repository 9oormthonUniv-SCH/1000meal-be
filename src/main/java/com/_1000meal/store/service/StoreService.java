package com._1000meal.store.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.service.MenuService;
import com._1000meal.store.domain.Store;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final MenuService menuService;

    @Transactional(readOnly = true)
    public StoreDetailedResponse getStoreDetail(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        WeeklyMenuResponse weeklyMenu = menuService.getWeeklyMenu(storeId,today);

        DailyMenu dm = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, today)
                .orElse(null);

        int remain = (dm != null && dm.getStock() != null) ? dm.getStock() : 0;
        boolean isOpen = (dm != null) && dm.isOpen();

        return store.toDetailedResponse(weeklyMenu, remain, isOpen);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {

        List<Long> storeIds = storeRepository.findAllStoreIds();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return storeIds.stream()
                .map(id -> {
                    Store store = storeRepository.findById(id)
                            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

                    DailyMenu dailyMenu = dailyMenuRepository.findDailyMenuByStoreIdAndDate(id, today)
                            .orElse(null);

                    DailyMenuDto todayMenuDto = (dailyMenu != null) ? dailyMenu.toDto() : null;

                    // 4. Store 엔티티의 메서드를 사용하여 DTO를 생성하고 반환합니다.
                    return store.toStoreResponse(todayMenuDto);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String toggleStoreStatus(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        boolean previousState = store.isOpen();
        store.toggleIsOpen(); // Store isOpen 토글

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, today)
                .ifPresent(dailyMenu -> {
                    // DailyMenu isOpen도 Store와 동일하게 토글
                    dailyMenu.toggleIsOpen();

                    // 가게가 닫힘→열림으로 바뀐 경우, stock 초기화
                    if (store.isOpen() && !previousState) {
                        dailyMenu.updateStock(100);
                    }
                });

        return "가게와 오늘의 메뉴 운영 상태가 업데이트 되었습니다.";
    }

    @Transactional
    public String setImageUrl(Long storeId, String imageUrl) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));
        store.updateImageUrl(imageUrl); // Dirty Checking으로 저장
        return store.getImageUrl();
    }

//    public StoreResponse createStore(StoreRequest request) {
//        Store store = Store.builder()
//                .name(request.getName()).address(request.getAddress())
//                .phone(request.getPhone()).description(request.getDescription())
//                .openTime(request.getOpenTime()).closeTime(request.getCloseTime())
//                .remain(request.getRemain()).hours(request.getHours())
//                .lat(request.getLat()).lng(request.getLng())
//                .isOpen(false).weeklyMenu(null)
//                .build();
//
//        Store saved = storeRepository.save(store);
//
//        return StoreResponse.from(saved);
//    }
//
//    public void storeIsOpen(Long id) {
//        Store store = storeRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));
//
//        store.setOpen(!store.isOpen());
//        storeRepository.save(store);
//    }



}
