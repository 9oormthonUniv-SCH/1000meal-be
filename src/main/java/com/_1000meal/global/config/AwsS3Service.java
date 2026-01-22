package com._1000meal.global.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.presign-ttl-seconds:600}")
    private long presignTtlSeconds;

    private final AmazonS3 amazonS3;

    // 업로드 결과(키 + URL + 메타)
    public record UploadedFile(
            String s3Key,
            String url,
            String originalName,
            String contentType,
            long size
    ) {}

    public record PresignedUpload(
            String s3Key,
            String url,
            String uploadUrl,
            Map<String, String> headers,
            String originalName,
            String contentType,
            long size
    ) {}

    /**
     * ✅ 도메인별 폴더(prefix) 지원 업로드
     * 예) prefix = "notices/123"
     */
    public List<UploadedFile> uploadFiles(String prefix, List<MultipartFile> multipartFiles) {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        String normalizedPrefix = normalizePrefix(prefix);

        List<UploadedFile> results = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일이 포함되어 있습니다.");
            }

            String key = createFileName(normalizedPrefix, file.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, key, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                // 업로드 실패
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
            }

            String url = amazonS3.getUrl(bucket, key).toString();

            results.add(new UploadedFile(
                    key,
                    url,
                    safeOriginalName(file.getOriginalFilename()),
                    file.getContentType(),
                    file.getSize()
            ));
        }

        return results;
    }

    /**
     * 파일명 난수화 + prefix 적용
     * 예) notices/123/uuid.jpg
     */
    public String createFileName(String prefix, String originalFileName) {
        return prefix + "/" + UUID.randomUUID() + getFileExtension(originalFileName);
    }

    public PresignedUpload createPresignedUpload(
            String prefix,
            String originalFileName,
            String contentType,
            long size
    ) {
        String normalizedPrefix = normalizePrefix(prefix);
        String key = createFileName(normalizedPrefix, originalFileName);
        Date expiration = new Date(System.currentTimeMillis() + presignTtlSeconds * 1000);

        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);
        request.setContentType(contentType);
        request.addRequestParameter("x-amz-acl", "public-read");

        String uploadUrl = amazonS3.generatePresignedUrl(request).toString();
        String url = amazonS3.getUrl(bucket, key).toString();

        return new PresignedUpload(
                key,
                url,
                uploadUrl,
                Map.of(
                        "Content-Type", contentType,
                        "x-amz-acl", "public-read"
                ),
                safeOriginalName(originalFileName),
                contentType,
                size
        );
    }

    // 확장자 추출
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일명이 없습니다.");
        }
        try {
            String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
            if (ext.length() > 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 확장자입니다. (" + fileName + ")");
            }
            return ext;
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }

    // prefix 정규화: 앞/뒤 슬래시 제거, null이면 uploads
    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return "uploads";
        String p = prefix.trim();
        while (p.startsWith("/")) p = p.substring(1);
        while (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        if (p.isBlank()) return "uploads";
        return p;
    }

    private String safeOriginalName(String name) {
        if (name == null) return null;
        return name.length() > 255 ? name.substring(0, 255) : name;
    }

    /**
     * key(=파일명/경로)로 삭제
     */
    public void deleteFile(String s3Key) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, s3Key));
    }
}
