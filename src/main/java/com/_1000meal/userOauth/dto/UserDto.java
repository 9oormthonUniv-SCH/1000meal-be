package com._1000meal.userOauth.dto;

import com._1000meal.userOauth.domain.User;
import com._1000meal.global.constant.Role;
import lombok.Getter;

@Getter
public class UserDto {

    private final String userID;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final Role role;

    public UserDto(User user) {
        this.userID = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
    }
}