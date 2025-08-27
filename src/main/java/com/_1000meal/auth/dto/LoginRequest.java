package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull Role role,
        @JsonProperty("user_id") @NotBlank String userId,  // ← user_id 로 받음
        @NotBlank String password
        // 선택: 클라이언트가 보내면 사용, 안 보내면 서버에서 계정의 role을 그대로 씀
) {}