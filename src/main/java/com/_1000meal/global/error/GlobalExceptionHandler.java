package com._1000meal.global.error;

import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* 1) @Valid 바인딩 오류 (DTO 필드 검증 실패) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ApiResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .toList();

        return ApiResponse.error(ErrorCode.VALIDATION_ERROR, fieldErrors);
    }

    /* 1-1) 폼 바인딩/쿼리 파라미터 바인딩 실패 */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
        List<ApiResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .toList();

        return ApiResponse.error(ErrorCode.VALIDATION_ERROR, fieldErrors);
    }

    /* 2) @Validated on @RequestParam/@PathVariable 등 제약 위반 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ApiResponse.FieldErrorDetail(
                        v.getPropertyPath() == null ? "" : v.getPropertyPath().toString(),
                        v.getInvalidValue(),
                        v.getMessage()
                ))
                .toList();

        return ApiResponse.error(ErrorCode.BAD_REQUEST, fieldErrors);
    }

    /* 3) JSON 파싱 실패 등 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleJsonParse(HttpMessageNotReadableException ex) {
        return ApiResponse.error(null, ErrorCode.BAD_REQUEST);
    }

    /* 4) 필수 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        var fieldError = List.of(new ApiResponse.FieldErrorDetail(
                ex.getParameterName(), null, "필수 파라미터가 누락되었습니다."
        ));
        return ApiResponse.error(ErrorCode.BAD_REQUEST, fieldError);
    }

    /* 5) 비즈니스 레벨 단순 검증 실패 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        var details = List.of(new ApiResponse.FieldErrorDetail(null, null, ex.getMessage()));
        return ApiResponse.error(ErrorCode.BAD_REQUEST, details);
    }

    /* 6) 상태 위반 (순서 위배/활성화 안됨 등) */
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Void> handleIllegalState(IllegalStateException ex) {
        var details = List.of(new ApiResponse.FieldErrorDetail(null, null, ex.getMessage()));
        return ApiResponse.error(ErrorCode.CONFLICT, details);
    }

    /* 7) 무결성 제약 위반 (Unique 등) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ApiResponse.error(null, ErrorCode.CONFLICT);
    }

    /* 8) 인증/인가 */
    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleAuthentication(AuthenticationException ex) {
        return ApiResponse.error(null, ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException ex) {
        return ApiResponse.error(null, ErrorCode.FORBIDDEN);
    }

    /* 8-1) JWT 구체 케이스 */
    @ExceptionHandler(ExpiredJwtException.class)
    public ApiResponse<Void> handleJwtExpired(ExpiredJwtException ex) {
        return ApiResponse.error(null, ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ApiResponse<Void> handleJwt(JwtException ex) {
        return ApiResponse.error(null, ErrorCode.UNAUTHORIZED);
    }

    /* 9) 마지막 세이프티넷 */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleEtc(Exception ex) {
        return ApiResponse.error(null, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    /* 10) CustomException */
    @ExceptionHandler(CustomException.class)
    public ApiResponse<Void> handleCustom(CustomException ex) {
        return ApiResponse.error(null, ex.getErrorCodeIfs());
    }

    /* ---------- util ---------- */
    private ApiResponse.FieldErrorDetail toDetail(FieldError fe) {
        return new ApiResponse.FieldErrorDetail(
                fe.getField(),
                fe.getRejectedValue(),
                fe.getDefaultMessage()
        );
    }
}