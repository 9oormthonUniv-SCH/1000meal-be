package com._1000meal.adminlogin.dto;

import lombok.Getter;

@Getter
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}