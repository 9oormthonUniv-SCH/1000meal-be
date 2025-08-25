package com._1000meal.adminlogin.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private String username;
    private String name;
    private String phoneNumber;
}