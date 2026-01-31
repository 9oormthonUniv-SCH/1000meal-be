package com._1000meal.store.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.store.dto.SetStoreImageUrlRequest;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.service.StoreService;
import com._1000meal.store.service.StoreViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Store", description = "매장 조회 및 관리 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StoreViewService storeViewService;

    @Operation(
            summary = "매장 상세 조회",
            description = "매장 ID를 통해 매장의 상세 정보와 오늘/주간 메뉴 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매장 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailedResponse> getStoreDetail(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId
    ) {
        StoreDetailedResponse response = storeService.getStoreDetail(storeId);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "매장 전체 목록 조회",
            description = """
                    모든 매장의 기본 정보를 조회합니다.

                    - view=live (기본값): 실시간 조회
                    - view=cached: 캐시된 목록 + 실시간 재고 덮어쓰기
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매장 목록 조회 성공")
    })
    @GetMapping
    public ApiResponse<List<StoreResponse>> getAllStores(
            @RequestParam(name = "view", required = false, defaultValue = "live")
            String view
    ) {
        List<StoreResponse> response = "cached".equalsIgnoreCase(view)
                ? storeViewService.getAllStoresView()
                : storeService.getAllStores();
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "매장 운영 상태 토글",
            description = "매장의 운영 상태(isOpen)를 토글하고, 오늘의 메뉴 운영 상태도 함께 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "운영 상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @PostMapping("/status/{storeId}")
    public ApiResponse<String> status(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId
    ) {
        String response = storeService.toggleStoreStatus(storeId);
        return ApiResponse.ok(response);
    }

    @Operation(
            summary = "매장 이미지 URL 설정",
            description = "매장 대표 이미지 URL을 설정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이미지 URL 설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @PostMapping("/image-url")
    public ApiResponse<String> setImageUrl(
            @RequestBody SetStoreImageUrlRequest req
    ) {
        String response = storeService.setImageUrl(req.getStoreId(), req.getImageUrl());
        return ApiResponse.ok(response);
    }

//    @Operation(
//            summary = "매장 목록 조회 (캐시 + 실시간 재고 반영)",
//            description = """
//                    매장 목록의 기본 정보는 캐시를 사용하고,
//                    오늘의 재고(stock)만 실시간 DB 값으로 덮어씁니다.
//                    """
//    )
//    @ApiResponses({
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매장 목록 조회 성공")
//    })
//    @Deprecated(forRemoval = false)
//    @GetMapping("/list")
//    public ResponseEntity<ApiResponse<List<StoreResponse>>> listStores() {
//        List<StoreResponse> list = storeViewService.getAllStoresView();
//        return ResponseEntity.ok(ApiResponse.success(list, SuccessCode.OK));
//    }
}
