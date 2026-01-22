package com._1000meal.notice.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.notice.dto.NoticeCreateRequest;
import com._1000meal.notice.dto.NoticeResponse;
import com._1000meal.notice.dto.NoticeUpdateRequest;
import com._1000meal.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "공지사항 목록 조회",
            description = "삭제되지 않은 공지사항을 (고정 여부 desc, 생성일 desc) 기준으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공")
    })
    @GetMapping
    public ApiResponse<List<NoticeResponse>> list() {
        List<NoticeResponse> response = noticeService.getAllNotice();
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "공지사항 단건 조회",
            description = "공지사항 ID로 공지사항을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ApiResponse<NoticeResponse> get(@PathVariable Long id) {
        NoticeResponse response = noticeService.getNotice(id);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "공지사항 생성",
            description = "공지사항을 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패")
    })
    @PostMapping
    public ApiResponse<NoticeResponse> create(
            @ModelAttribute @Valid NoticeCreateRequest req,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        NoticeResponse response = noticeService.create(req, files);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "공지사항 수정",
            description = "공지사항 ID에 해당하는 공지사항을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ApiResponse<NoticeResponse> update(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid NoticeUpdateRequest request
    ) {
        NoticeResponse response = noticeService.update(id, request);
        return ApiResponse.success(response, SuccessCode.UPDATED);
    }

    @Operation(
            summary = "공지사항 삭제(소프트 삭제)",
            description = "공지사항을 소프트 삭제 처리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id
    ) {
        noticeService.delete(id);
        return ApiResponse.success(null, SuccessCode.OK);
    }
}
