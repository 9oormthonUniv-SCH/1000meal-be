package com._1000meal.admin.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginResponse {
    private String accessToken;
    private Long adminId;
    private String username;
    private String name;
    private String phoneNumber;
}