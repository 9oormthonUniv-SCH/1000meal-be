package com._1000meal.auth.dto;


import lombok.Getter; import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class LoginRequest {
    private String username;   // 학생=학번(userId), 관리자=username (원하면 이메일도 허용 가능)
    private String password;
}
