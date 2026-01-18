package com._1000meal.notice.repository;

import com._1000meal.notice.domain.NoticeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeImageRepository extends JpaRepository<NoticeImage, Long> {
    List<NoticeImage> findByNoticeIdOrderByIdAsc(Long noticeId);
}