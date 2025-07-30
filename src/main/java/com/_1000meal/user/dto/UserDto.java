package com._1000meal.user.dto;

import com._1000meal.user.domain.User;
import com._1000meal.user.domain.Role;
import lombok.Getter;

@Getter
public class UserDto {

    private final String userID;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final Role role;

    public UserDto(User user) {
        this.userID = user.getUserID();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
    }
}