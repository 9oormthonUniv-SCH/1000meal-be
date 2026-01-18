package com._1000meal.notice.dto;

public record NoticeImageResponse(
        Long id,
        String url,
        String originalName,
        String contentType,
        long size
) {}