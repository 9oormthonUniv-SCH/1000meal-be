package com._1000meal.store.service;

import com._1000meal.store.domain.Store;
import com._1000meal.store.dto.StoreRequest;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com._1000meal.menu.domain.Menu;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public StoreResponse createStore(StoreRequest request) {
        Store store = Store.builder().name(request.getName()).address(request.getAddress()).phone(request.getPhone()).description(request.getDescription()).openTime(request.getOpenTime()).closeTime(request.getCloseTime()).remain(request.getRemain()).hours(request.getHours()).lat(request.getLat()).lng(request.getLng())
                .isOpen(false).menus(null)
                .build();

        Store saved = storeRepository.save(store);

        return StoreResponse.builder().id(saved.getId()).name(saved.getName()).address(saved.getAddress()).phone(saved.getPhone()).description(saved.getDescription()).hours(saved.getHours()).remain(saved.getRemain()).lat(saved.getLat()).lng(saved.getLng())
                .isOpen(false)
                .menu(
                        saved.getMenus() == null
                                ? new ArrayList<>()
                                : saved.getMenus().stream()
                                .map(Menu::getImageUrl)
                                .toList()
                )
                .build();
    }


    private StoreResponse toDto(Store store) {
        boolean isOpenNow = isStoreOpen(store);
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .description(store.getDescription())
                .hours(store.getHours())
                .remain(store.getRemain())
                .lat(store.getLat())
                .lng(store.getLng())
                .isOpen(isOpenNow)
                .menu(store.getMenus().stream()
                        .map(menu -> menu.getImageUrl()) // 또는 OCR 추출된 이름이 있다면 대체
                        .toList())
                .build();
    }

    private boolean isStoreOpen(Store store) {
        LocalTime now = LocalTime.now();
        return now.isAfter(store.getOpenTime()) && now.isBefore(store.getCloseTime());
    }
}
