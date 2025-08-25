package com._1000meal.user.signup.dto;

import com._1000meal.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSignupResponse {
    private String userId;
    private String name;
    private String email;
    private String role;  // STUDENT

    public static UserSignupResponse from(User user) {
        return new UserSignupResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}