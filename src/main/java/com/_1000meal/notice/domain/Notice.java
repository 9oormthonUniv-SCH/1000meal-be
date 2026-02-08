package com._1000meal.notice.domain;

import com._1000meal.notice.dto.NoticeImageResponse;
import com._1000meal.notice.dto.NoticeResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notices")
public class Notice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, Boolean isPublished, Boolean isPinned) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (isPublished != null) this.isPublished = isPublished;
        if (isPinned != null) this.isPinned = isPinned;
    }

    public void softDelete() { this.deletedAt = LocalDateTime.now(); }

    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static NoticeResponse toResponse(Notice n) {
        return toResponse(n, List.of());
    }

    public static NoticeResponse toResponse(Notice n, List<NoticeImageResponse> images) {
        final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new NoticeResponse(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.isPublished(),
                n.isPinned(),
                n.getCreatedAt().format(F),
                n.getUpdatedAt().format(F),
                images
        );
    }
}
