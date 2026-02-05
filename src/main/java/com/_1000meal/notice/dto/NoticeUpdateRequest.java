package com._1000meal.notice.dto;

import jakarta.validation.constraints.Size;

public record NoticeUpdateRequest(
        @Size(max = 200) String title,
        String content,
        Boolean isPublished,
        Boolean isPinned
) {}
