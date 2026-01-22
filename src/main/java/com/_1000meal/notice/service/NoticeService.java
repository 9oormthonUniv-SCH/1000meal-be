package com._1000meal.notice.service;

import com._1000meal.global.error.exception.CustomException;
import com._1000meal.notice.domain.Notice;
import com._1000meal.notice.dto.NoticeCreateRequest;
import com._1000meal.notice.dto.NoticeImagePresignRequest;
import com._1000meal.notice.dto.NoticeImagePresignResponse;
import com._1000meal.notice.dto.NoticeImageRegisterRequest;
import com._1000meal.notice.dto.NoticeImageResponse;
import com._1000meal.notice.dto.NoticeResponse;
import com._1000meal.notice.dto.NoticeUpdateRequest;
import com._1000meal.notice.repository.NoticeRepository;
import com._1000meal.global.error.code.NoticeErrorCode; // 가정: 존재
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com._1000meal.notice.domain.Notice.toResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageService noticeImageService;

    @Transactional(readOnly = true)
    public List<NoticeResponse> getAllNotice() {
        return noticeRepository.findAllByDeletedAtIsNull(
                        Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt"))
                ).stream()
                .map(Notice::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notice not found"));

        List<NoticeImageResponse> images = noticeImageService.list(noticeId);
        return toResponse(notice, images);
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
        return toResponse(saved, List.of());
    }

    @Transactional(readOnly = true)
    public List<NoticeImagePresignResponse> presignImages(
            Long noticeId,
            NoticeImagePresignRequest request
    ) {
        return noticeImageService.presign(noticeId, request);
    }

    @Transactional
    public List<NoticeImageResponse> registerImages(
            Long noticeId,
            NoticeImageRegisterRequest request
    ) {
        return noticeImageService.register(noticeId, request);
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
