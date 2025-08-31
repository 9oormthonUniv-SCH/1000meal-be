package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

//    @PostMapping("/weekly")
//    public ResponseEntity<WeeklyMenuResponse> createWeeklyMenu(@RequestBody WeeklyMenuRequest request) {
//        menuService.createWeeklyMenu(request);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }

    @PostMapping("/daily/{storeId}")
    public ApiResponse<DailyMenuAddRequest> createDaillyMenu(
            @PathVariable Long storeId,
            @RequestBody DailyMenuAddRequest request) {

        menuService.addOrReplaceDailyMenu(storeId,request);

        return ApiResponse.success(request, SuccessCode.OK);
    }


    @GetMapping("/weekly/{storeId}")
    public ApiResponse<WeeklyMenuResponse> getWeeklyMenu(@PathVariable Long storeId) {
        WeeklyMenuResponse response = menuService.getWeeklyMenu(storeId);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @GetMapping("/daily/{storeId}")
    public ApiResponse<DailyMenuDto> getDailyMenu(
            @PathVariable Long storeId,
            @RequestParam LocalDate date
    ) {
        DailyMenuDto resp = menuService.getDailyMenu(storeId, date);
        return ApiResponse.success(resp, SuccessCode.OK);
    }

    @PatchMapping("/daily/deduct/{menuId}")
    public ApiResponse<?> deductStock(
            @PathVariable Long menuId,
            DeductionUnit deductionUnit
    ) {
        StockResponse response = menuService.deductStock(menuId, deductionUnit.getValue());
        return ApiResponse.ok(response);
    }

    @PostMapping("/daily/stock/{menuId}")
    public ApiResponse<?> updateStock(
            @PathVariable Long menuId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        StockResponse response = menuService.operationStock(menuId, request.getStock());

        return ApiResponse.success(response, SuccessCode.UPDATED);
    }
}
