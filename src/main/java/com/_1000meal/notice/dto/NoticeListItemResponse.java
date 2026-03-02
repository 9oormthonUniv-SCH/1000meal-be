package com._1000meal.notice.dto;

public record NoticeListItemResponse(
        Long id,
        String title,
        String content,
        boolean isPublished,
        boolean isPinned,
        boolean hasImage,
        String createdAt,
        String updatedAt
) {}
