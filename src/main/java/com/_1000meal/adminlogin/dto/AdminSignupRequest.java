package com._1000meal.adminlogin.dto;


import lombok.Getter;

@Getter
public class AdminSignupRequest {
    private String username;
    private String password;
    private String name;
    private String phoneNumber;
}