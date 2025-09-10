package com._1000meal.notice.dto;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        boolean isPublished,
        boolean isPinned,
        String createdAt,
        String updatedAt
) {}
