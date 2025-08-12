package com._1000meal.user.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class UserLoginRequest {
    private String userId;
    private String password;
}