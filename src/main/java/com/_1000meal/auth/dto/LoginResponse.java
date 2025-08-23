// com._1000meal.auth.dto.LoginResponse
package com._1000meal.auth.dto;

import lombok.AllArgsConstructor; import lombok.Getter;


@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String role;               // STUDENT / ADMIN
    private String userIdOrUsername;   // 학생=학번, 관리자=username
    private String name;
    private String email;              // 학생은 학교이메일, 관리자는 null/관리자이메일
}