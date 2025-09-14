package com._1000meal.auth.dto.EmailChange;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailChangeRequestCodeRequest(
        @NotBlank String changeId,
        @NotBlank
        @Email
        @Pattern(regexp = "^[0-9A-Za-z._%+-]+@sch\\.ac\\.kr$", message = "학교 이메일(@sch.ac.kr)만 사용할 수 있어요.")
        String newEmail
) {}