package com._1000meal.auth.model;

public enum AccountStatus {
    ACTIVE,     // 정상 활성화
    PENDING,    // 이메일 인증 대기
    SUSPENDED,  // 정지 / 비활성화
    DELETED;
}