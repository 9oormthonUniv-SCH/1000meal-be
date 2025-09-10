package com._1000meal.notice.repository;

import com._1000meal.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String t, String c, Pageable pageable);
}