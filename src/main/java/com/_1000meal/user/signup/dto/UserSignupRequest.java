package com._1000meal.user.signup.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class UserSignupRequest {
    private String userId;   // 학번/아이디
    private String name;
    private String email;    // @sch.ac.kr
    private String password; // 평문으로 들어옴
}