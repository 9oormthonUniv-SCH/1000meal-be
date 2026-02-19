package com._1000meal.notice.repository;

import com._1000meal.notice.domain.NoticeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeImageRepository extends JpaRepository<NoticeImage, Long> {
    List<NoticeImage> findByNoticeIdOrderByIdAsc(Long noticeId);

    @Query("SELECT DISTINCT ni.notice.id FROM NoticeImage ni WHERE ni.notice.id IN :noticeIds")
    List<Long> findNoticeIdsWithImagesIn(@Param("noticeIds") List<Long> noticeIds);
}
