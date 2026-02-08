package com._1000meal.notice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Notice 쪽 엔티티 경로는 너 프로젝트에 맞게 수정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "s3_key", nullable = false, length = 600)
    private String s3Key;

    @Column(name = "url", nullable = false, length = 1200)
    private String url;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static NoticeImage of(
            Notice notice,
            String s3Key,
            String url,
            String originalName,
            String contentType,
            long size
    ) {
        NoticeImage ni = new NoticeImage();
        ni.notice = notice;
        ni.s3Key = s3Key;
        ni.url = url;
        ni.originalName = originalName;
        ni.contentType = contentType;
        ni.size = size;
        return ni;
    }
}
