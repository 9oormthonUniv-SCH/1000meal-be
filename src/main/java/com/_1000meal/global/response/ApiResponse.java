package com._1000meal.global.response;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.error.exception.ErrorCodeIfs;

import java.util.List;

public record ApiResponse<T>(
        T data,
        Result result,                       // ← 콤마 보정
        List<FieldErrorDetail> errors        // ← 새 필드 (nullable)
) {
    // 200 OK (기존 시그니처 유지)
    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(
                data,
                Result.ok(),
                null                                  // ← errors 기본 null
        );
    }

    // other 2xx (기존 시그니처 유지)
    public static <T> ApiResponse<T> success(T data, SuccessCode successCode) {
        return new ApiResponse<>(
                data,
                Result.success(successCode),
                null
        );
    }

    // error (기존 시그니처 유지)
    public static <T> ApiResponse<T> error(T data, ErrorCodeIfs errorCodeIfs) {
        return new ApiResponse<>(
                data,
                Result.error(errorCodeIfs),
                null
        );
    }

    // ✅ 신규(비파괴) 오버로드: 필드 단위 에러 상세 포함하고 싶을 때만 사용
    public static <T> ApiResponse<T> error(ErrorCodeIfs errorCodeIfs, List<FieldErrorDetail> errors) {
        return new ApiResponse<>(
                null,
                Result.error(errorCodeIfs),
                errors
        );
    }

    // 필드 에러 한 건 표현용
    public record FieldErrorDetail(String field, Object rejectedValue, String reason) {}
}