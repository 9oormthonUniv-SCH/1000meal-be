package com._1000meal.store.service;

import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreRequest;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final WeeklyMenuRepository weeklyMenuRepository;

    public List<StoreDetailedResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(store -> {
                    WeeklyMenu weeklyMenu = weeklyMenuRepository.findByStore(store)
                            .orElse(null);
                    return StoreDetailedResponse.from(store, weeklyMenu);
                })
                .toList();
    }

    public StoreResponse createStore(StoreRequest request) {
        Store store = Store.builder()
                .name(request.getName()).address(request.getAddress())
                .phone(request.getPhone()).description(request.getDescription())
                .openTime(request.getOpenTime()).closeTime(request.getCloseTime())
                .remain(request.getRemain()).hours(request.getHours())
                .lat(request.getLat()).lng(request.getLng())
                .isOpen(false).weeklyMenu(null)
                .build();

        Store saved = storeRepository.save(store);

        return StoreResponse.from(saved);
    }

    public void storeIsOpen(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));

        store.setOpen(!store.isOpen());
        storeRepository.save(store);
    }

    public StoreDetailedResponse getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 가게입니다."));

        WeeklyMenu weeklyMenu = weeklyMenuRepository.findByStore(store)
                .orElseThrow(() -> new RuntimeException("해당 가게의 주간 메뉴가 없습니다."));

        return StoreDetailedResponse.from(store, weeklyMenu);
    }

}
