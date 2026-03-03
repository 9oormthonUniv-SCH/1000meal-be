package com._1000meal.qr.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.qr.api.dto.QrStoreResponse;
import com._1000meal.qr.service.QrStoreQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qr/stores")
public class QrStoreQueryController {

    private final QrStoreQueryService qrStoreQueryService;

    // TODO: 운영 전환 시 ADMIN 권한으로 제한 가능 (@PreAuthorize("hasRole('ADMIN')"))
    @GetMapping("/{qrToken}")
    public ApiResponse<QrStoreResponse> getByQrToken(@PathVariable String qrToken) {
        QrStoreResponse response = qrStoreQueryService.getByQrToken(qrToken);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    // TODO: 운영 전환 시 ADMIN 권한으로 제한 가능 (@PreAuthorize("hasRole('ADMIN')"))
    @GetMapping
    public ApiResponse<List<QrStoreResponse>> getAll() {
        List<QrStoreResponse> response = qrStoreQueryService.getAll();
        return ApiResponse.success(response, SuccessCode.OK);
    }
}
