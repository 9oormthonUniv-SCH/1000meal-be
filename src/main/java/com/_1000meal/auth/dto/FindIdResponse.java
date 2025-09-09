package com._1000meal.auth.dto;


public record FindIdResponse(
        String message,
        String userId
) {
    public static FindIdResponse of(String userId) {
        return new FindIdResponse("해당 이메일로 가입된 학번(아이디) 입니다.", userId);
    }
}