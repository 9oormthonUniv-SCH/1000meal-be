package com._1000meal.auth.dto;


import lombok.Builder;

public record ChangePasswordResponse(
        String message
) {
    @Builder public ChangePasswordResponse {}
}