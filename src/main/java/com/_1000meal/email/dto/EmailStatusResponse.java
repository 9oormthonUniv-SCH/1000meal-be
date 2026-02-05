package com._1000meal.email.dto;

// 상태 조회 응답 DTO (선택)
public record EmailStatusResponse(String email, boolean verified) {}