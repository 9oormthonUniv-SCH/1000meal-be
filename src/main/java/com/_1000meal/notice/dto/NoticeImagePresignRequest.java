package com._1000meal.notice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NoticeImagePresignRequest(
        @NotEmpty @Size(max = 10) List<@Valid FileMeta> files
) {
    public record FileMeta(
            @NotBlank String originalName,
            @NotBlank String contentType,
            @Positive long size
    ) {}
}
