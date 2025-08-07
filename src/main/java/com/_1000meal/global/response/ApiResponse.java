package com._1000meal.global.response;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.error.exception.ErrorCodeIfs;

// 일관성(에러 응답, 성공 응답), 클라이언트 데이터 처리, 유지 보수, 모니터링 강화를 위해 공통 응답을 사용
public record ApiResponse<T>(
        T data,
        Result result
) {
    // 200 ok
    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(
                data,
                Result.ok()
        );
    }

    // other 2xx code
    public static <T> ApiResponse<T> success(T data, SuccessCode successCode) {
        return new ApiResponse<>(
                data,
                Result.success(successCode)
        );
    }

    // error response
    public static <T> ApiResponse<T> error(T data, ErrorCodeIfs errorCodeIfs) {
        return new ApiResponse<>(
                data,
                Result.error(errorCodeIfs)
        );
    }
}