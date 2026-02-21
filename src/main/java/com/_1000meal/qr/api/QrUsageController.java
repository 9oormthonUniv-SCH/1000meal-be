package com._1000meal.qr.api;

import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.qr.api.dto.QrUsageRequest;
import com._1000meal.qr.api.dto.QrUsageResponse;
import com._1000meal.qr.api.dto.TodayQrUsageResponse;
import com._1000meal.qr.service.QrUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/qr")
public class QrUsageController {

    private final QrUsageService qrUsageService;

    @PostMapping("/usages")
    public ApiResponse<QrUsageResponse> createUsage(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody QrUsageRequest request
    ) {
        QrUsageResponse response = qrUsageService.createUsage(principal.id(), request.qrToken());
        return ApiResponse.success(response, SuccessCode.CREATED);
    }

    @GetMapping("/usages/today")
    public ApiResponse<TodayQrUsageResponse> getTodayUsage(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        TodayQrUsageResponse response = qrUsageService.getTodayUsage(principal.id());
        return ApiResponse.success(response, SuccessCode.OK);
    }
}
