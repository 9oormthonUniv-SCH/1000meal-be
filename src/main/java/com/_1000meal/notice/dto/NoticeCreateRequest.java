package com._1000meal.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        boolean isPublished,
        boolean isPinned
) {}

