package com._1000meal.notice.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.notice.dto.NoticeCreateRequest;
import com._1000meal.notice.dto.NoticeImagePresignRequest;
import com._1000meal.notice.dto.NoticeImagePresignResponse;
import com._1000meal.notice.dto.NoticeImageRegisterRequest;
import com._1000meal.notice.dto.NoticeImageResponse;
import com._1000meal.notice.dto.NoticeResponse;
import com._1000meal.notice.dto.NoticeUpdateRequest;
import com._1000meal.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
            description = "공지사항을 생성합니다. 이미지 업로드는 presign + 등록 API로 분리됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공지사항 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                    {
                                      "data": {
                                        "id": 123,
                                        "title": "공지 제목",
                                        "content": "공지 내용",
                                        "isPublished": true,
                                        "isPinned": false,
                                        "createdAt": "2026-01-22T10:00:00",
                                        "updatedAt": "2026-01-22T10:00:00",
                                        "images": []
                                      },
                                      "result": {
                                        "code": "OK",
                                        "message": "성공",
                                        "timestamp": "2026-01-22T10:00:00"
                                      },
                                      "errors": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            )
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
            summary = "공지 이미지 Presigned URL 발급",
            description = "공지사항 ID 기준으로 이미지 업로드용 Presigned URL을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                    {
                                      "data": [
                                        {
                                          "s3Key": "notices/123/uuid1.jpg",
                                          "url": "https://bucket.s3.ap-northeast-2.amazonaws.com/notices/123/uuid1.jpg",
                                          "uploadUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/notices/123/uuid1.jpg?X-Amz-Algorithm=...",
                                          "headers": {
                                            "Content-Type": "image/jpeg",
                                            "x-amz-acl": "public-read"
                                          },
                                          "originalName": "photo1.jpg",
                                          "contentType": "image/jpeg",
                                          "size": 345678
                                        }
                                      ],
                                      "result": {
                                        "code": "OK",
                                        "message": "성공",
                                        "timestamp": "2026-01-22T10:00:01"
                                      },
                                      "errors": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공지사항을 찾을 수 없음"
            )
    })
    @PostMapping("/{id}/images/presign")
    public ApiResponse<List<NoticeImagePresignResponse>> presignImages(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NoticeImagePresignRequest.class),
                            examples = @ExampleObject(
                                    name = "request",
                                    value = """
                                    {
                                      "files": [
                                        {
                                          "originalName": "photo1.jpg",
                                          "contentType": "image/jpeg",
                                          "size": 345678
                                        },
                                        {
                                          "originalName": "photo2.png",
                                          "contentType": "image/png",
                                          "size": 123456
                                        }
                                      ]
                                    }
                                    """
                            )
                    )
            )
            @RequestBody @Valid NoticeImagePresignRequest request
    ) {
        List<NoticeImagePresignResponse> response =
                noticeService.presignImages(id, request);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "공지 이미지 메타 등록",
            description = "Presigned URL로 업로드 완료된 이미지 메타데이터를 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이미지 메타 등록 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                    {
                                      "data": [
                                        {
                                          "id": 10,
                                          "url": "https://bucket.s3.ap-northeast-2.amazonaws.com/notices/123/uuid1.jpg",
                                          "originalName": "photo1.jpg",
                                          "contentType": "image/jpeg",
                                          "size": 345678
                                        }
                                      ],
                                      "result": {
                                        "code": "OK",
                                        "message": "성공",
                                        "timestamp": "2026-01-22T10:00:02"
                                      },
                                      "errors": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공지사항을 찾을 수 없음"
            )
    })
    @PostMapping("/{id}/images")
    public ApiResponse<List<NoticeImageResponse>> registerImages(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NoticeImageRegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "request",
                                    value = """
                                    {
                                      "images": [
                                        {
                                          "s3Key": "notices/123/uuid1.jpg",
                                          "url": "https://bucket.s3.ap-northeast-2.amazonaws.com/notices/123/uuid1.jpg",
                                          "originalName": "photo1.jpg",
                                          "contentType": "image/jpeg",
                                          "size": 345678
                                        }
                                      ]
                                    }
                                    """
                            )
                    )
            )
            @RequestBody @Valid NoticeImageRegisterRequest request
    ) {
        List<NoticeImageResponse> response =
                noticeService.registerImages(id, request);
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
