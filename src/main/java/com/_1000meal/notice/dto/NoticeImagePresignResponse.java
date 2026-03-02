package com._1000meal.notice.dto;

import java.util.Map;

public record NoticeImagePresignResponse(
        String s3Key,
        String url,
        String uploadUrl,
        Map<String, String> headers,
        String originalName,
        String contentType,
        long size
) {}
