package com._1000meal.holiday.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.holiday.dto.HolidayResponse;
import com._1000meal.holiday.dto.HolidayUpdateRequest;
import com._1000meal.holiday.dto.HolidayUpsertRequest;
import com._1000meal.holiday.service.HolidaySyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Holiday", description = "관리자 전용 휴일 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/holidays")
public class HolidayController {

    private final HolidaySyncService holidaySyncService;

    @Operation(summary = "연도별 휴일 목록 조회")
    @GetMapping
    public ApiResponse<List<HolidayResponse>> getHolidays(@RequestParam int year) {
        return ApiResponse.ok(holidaySyncService.getHolidaysByYear(year));
    }

    @Operation(summary = "휴일 직접 등록")
    @PostMapping
    public ApiResponse<HolidayResponse> createHoliday(@Valid @RequestBody HolidayUpsertRequest request) {
        return ApiResponse.success(holidaySyncService.createHoliday(request), SuccessCode.CREATED);
    }

    @Operation(summary = "휴일 이름 수정")
    @PatchMapping("/{holidayId}")
    public ApiResponse<HolidayResponse> updateHoliday(
            @PathVariable Long holidayId,
            @Valid @RequestBody HolidayUpdateRequest request
    ) {
        return ApiResponse.success(holidaySyncService.updateHoliday(holidayId, request), SuccessCode.UPDATED);
    }

    @Operation(summary = "휴일 삭제")
    @DeleteMapping("/{holidayId}")
    public ApiResponse<Void> deleteHoliday(@PathVariable Long holidayId) {
        holidaySyncService.deleteHoliday(holidayId);
        return ApiResponse.success(null, SuccessCode.OK);
    }

    @Operation(summary = "연도별 휴일 외부 API 동기화")
    @PostMapping("/sync")
    public ApiResponse<Void> syncHolidays(@RequestParam int year) {
        holidaySyncService.syncYear(year);
        return ApiResponse.success(null, SuccessCode.OK);
    }
}
