package com._1000meal.notice.image.service;

import com._1000meal.global.config.AwsS3Service;
import com._1000meal.notice.domain.Notice;
import com._1000meal.notice.domain.NoticeImage;
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

    // 필요하면 gif까지 열어줘도 됨
    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");

    @Transactional
    public List<NoticeImageResponse> upload(Long noticeId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files is empty");
        }
        if (files.size() > MAX_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "too many files (max " + MAX_COUNT + ")");
        }

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notice not found"));

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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "image upload failed");
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "db save failed after s3 upload");
        }
    }

    @Transactional
    public void delete(Long noticeId, Long imageId) {
        NoticeImage img = noticeImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "image not found"));

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
}
