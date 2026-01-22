package com._1000meal.notice.service;

import com._1000meal.global.config.AwsS3Service;
import com._1000meal.notice.domain.Notice;
import com._1000meal.notice.domain.NoticeImage;
import com._1000meal.notice.dto.NoticeImagePresignRequest;
import com._1000meal.notice.dto.NoticeImagePresignResponse;
import com._1000meal.notice.dto.NoticeImageRegisterRequest;
import com._1000meal.notice.dto.NoticeImageResponse;
import com._1000meal.notice.repository.NoticeImageRepository;
import com._1000meal.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeImageService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final AwsS3Service awsS3Service;

    private static final int MAX_COUNT = 10;
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Transactional
    public List<NoticeImageResponse> upload(Long noticeId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비었습니다.");
        }
        if (files.size() > MAX_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 최대 " + MAX_COUNT + "장까지 추가 가능합니다.");
        }

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."));

        validateFiles(files);

        // ✅ S3 업로드 (도메인별 prefix)
        String prefix = "notices/" + noticeId;

        List<AwsS3Service.UploadedFile> uploaded = null;
        try {
            uploaded = awsS3Service.uploadFiles(prefix, files);
        } catch (ResponseStatusException e) {
            // AwsS3Service에서 이미 적절한 상태코드로 던짐
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패하였습니다.");
        }

        // ✅ DB 저장 + 보상삭제(여기서 실패하면 S3에 올라간 것들 제거)
        try {
            List<NoticeImageResponse> responses = new ArrayList<>();

            for (AwsS3Service.UploadedFile u : uploaded) {
                NoticeImage saved = noticeImageRepository.save(
                        NoticeImage.of(
                                notice,
                                u.s3Key(),
                                u.url(),
                                u.originalName(),
                                u.contentType(),
                                u.size()
                        )
                );

                responses.add(new NoticeImageResponse(
                        saved.getId(),
                        saved.getUrl(),
                        saved.getOriginalName(),
                        saved.getContentType(),
                        saved.getSize()
                ));
            }

            return responses;

        } catch (Exception e) {
            // ✅ 보상 삭제: DB 저장이 중간에 실패한 경우 S3 정리
            for (AwsS3Service.UploadedFile u : uploaded) {
                try { awsS3Service.deleteFile(u.s3Key()); } catch (Exception ignore) {}
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 저장에 실패하였습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<NoticeImagePresignResponse> presign(
            Long noticeId,
            NoticeImagePresignRequest request
    ) {
        if (request == null || request.files() == null || request.files().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files required");
        }
        if (request.files().size() > MAX_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "too many files (max " + MAX_COUNT + ")");
        }

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "notice not found"));

        List<NoticeImagePresignResponse> responses = new ArrayList<>();
        String prefix = "notices/" + notice.getId();

        for (NoticeImagePresignRequest.FileMeta f : request.files()) {
            validateFileMeta(f.originalName(), f.contentType(), f.size());

            AwsS3Service.PresignedUpload presigned =
                    awsS3Service.createPresignedUpload(
                            prefix,
                            f.originalName(),
                            f.contentType(),
                            f.size()
                    );

            responses.add(new NoticeImagePresignResponse(
                    presigned.s3Key(),
                    presigned.url(),
                    presigned.uploadUrl(),
                    presigned.headers(),
                    presigned.originalName(),
                    presigned.contentType(),
                    presigned.size()
            ));
        }

        return responses;
    }

    @Transactional
    public List<NoticeImageResponse> register(
            Long noticeId,
            NoticeImageRegisterRequest request
    ) {
        if (request == null || request.images() == null || request.images().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "images required");
        }
        if (request.images().size() > MAX_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "too many images (max " + MAX_COUNT + ")");
        }

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "notice not found"));

        String prefix = "notices/" + notice.getId() + "/";
        List<NoticeImageResponse> responses = new ArrayList<>();

        for (NoticeImageRegisterRequest.ImageMeta img : request.images()) {
            validateFileMeta(img.originalName(), img.contentType(), img.size());
            if (img.s3Key() == null || !img.s3Key().startsWith(prefix)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid s3Key");
            }
            if (img.url() == null || img.url().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url required");
            }

            NoticeImage saved = noticeImageRepository.save(
                    NoticeImage.of(
                            notice,
                            img.s3Key(),
                            img.url(),
                            img.originalName(),
                            img.contentType(),
                            img.size()
                    )
            );

            responses.add(new NoticeImageResponse(
                    saved.getId(),
                    saved.getUrl(),
                    saved.getOriginalName(),
                    saved.getContentType(),
                    saved.getSize()
            ));
        }

        return responses;
    }

    @Transactional
    public void delete(Long noticeId, Long imageId) {
        NoticeImage img = noticeImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."));

        if (!img.getNotice().getId().equals(noticeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image does not belong to notice");
        }

        // 1) S3 삭제
        try {
            awsS3Service.deleteFile(img.getS3Key());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "s3 delete failed");
        }

        // 2) DB 삭제
        noticeImageRepository.delete(img);
    }

    @Transactional(readOnly = true)
    public List<NoticeImageResponse> list(Long noticeId) {
        return noticeImageRepository.findByNoticeIdOrderByIdAsc(noticeId)
                .stream()
                .map(i -> new NoticeImageResponse(
                        i.getId(),
                        i.getUrl(),
                        i.getOriginalName(),
                        i.getContentType(),
                        i.getSize()
                ))
                .toList();
    }

    private void validateFiles(List<MultipartFile> files) {
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty file exists");
            }
            if (f.getSize() > MAX_SIZE_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file too large (max 5MB)");
            }
            String ct = f.getContentType();
            if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid contentType: " + ct);
            }
        }
    }

    private void validateFileMeta(String originalName, String contentType, long size) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "invalid contentType: " + contentType);
        }
        if (size <= 0 || size > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "file too large (max 5MB)");
        }
        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "originalName required");
        }
    }
}
