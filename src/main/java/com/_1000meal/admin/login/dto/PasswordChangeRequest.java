package com._1000meal.admin.login.dto;

import lombok.Getter;

@Getter
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}