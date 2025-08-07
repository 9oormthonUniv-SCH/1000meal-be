package com._1000meal.admin.login.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {
    private String username;
    private String password;
}