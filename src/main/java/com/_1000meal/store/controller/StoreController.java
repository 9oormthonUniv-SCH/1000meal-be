package com._1000meal.store.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.store.dto.SetStoreImageUrlRequest;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.service.StoreService;
import com._1000meal.store.service.StoreViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StoreViewService storeViewService;

    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailedResponse> getStoreDetail(@PathVariable Long storeId) {

        StoreDetailedResponse response = storeService.getStoreDetail(storeId);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @GetMapping
    public ApiResponse<List<StoreResponse>> getAllStores() {

        List<StoreResponse> response = storeService.getAllStores();

        return ApiResponse.success(response, SuccessCode.OK);
    }

    @PostMapping("/status/{storeId}")
    public ApiResponse<?> status(@PathVariable Long storeId) {
        String response = storeService.toggleStoreStatus(storeId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/image-url")
    public ApiResponse<?> setImageUrl(@RequestBody SetStoreImageUrlRequest req) {
        String response = storeService.setImageUrl(req.getStoreId(), req.getImageUrl());
        return ApiResponse.ok(response);
    }

//    @PostMapping
//    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request) {
//        StoreResponse response = storeService.createStore(request);
//        return ResponseEntity.status(201).body(response);
//    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> listStores() {
        var list = storeViewService.getAllStoresView();
        return ResponseEntity.ok(ApiResponse.success(list, SuccessCode.OK));
    }

}