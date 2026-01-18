package com._1000meal.notice.service;

import com._1000meal.global.error.exception.CustomException;
import com._1000meal.notice.domain.Notice;
import com._1000meal.notice.dto.NoticeCreateRequest;
import com._1000meal.notice.dto.NoticeResponse;
import com._1000meal.notice.dto.NoticeUpdateRequest;
import com._1000meal.notice.repository.NoticeRepository;
import com._1000meal.global.error.code.NoticeErrorCode; // 가정: 존재
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com._1000meal.notice.domain.Notice.toResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<NoticeResponse> list() {
        return noticeRepository.findAllByDeletedAtIsNull(
                        Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt"))
                ).stream()
                .map(Notice::toResponse)
                .toList();
    }

    public NoticeResponse get(Long id) {
        Notice n = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.NOTICE_NOT_FOUND));
        return toResponse(n);
    }

    @Transactional
    public NoticeResponse create(NoticeCreateRequest req) {
        Notice n = Notice.builder()
                .title(req.title())
                .content(req.content())
                .isPublished(req.isPublished())
                .isPinned(req.isPinned())
                .build();

        Notice saved = noticeRepository.save(n);
        return toResponse(saved);
    }

    @Transactional
    public NoticeResponse update(Long id, NoticeUpdateRequest req) {
        Notice n = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.NOTICE_NOT_FOUND));
        n.update(req.title(), req.content(), req.isPublished(), req.isPinned());
        return toResponse(n);
    }

    @Transactional
    public void delete(Long id) {
        Notice n = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.NOTICE_NOT_FOUND));
        n.softDelete();
    }
}
