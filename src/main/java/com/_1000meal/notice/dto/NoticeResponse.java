package com._1000meal.notice.dto;

import java.util.List;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        boolean isPublished,
        boolean isPinned,
        String createdAt,
        String updatedAt,
        List<NoticeImageResponse> images
) {}
