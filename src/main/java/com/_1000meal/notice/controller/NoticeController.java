package com._1000meal.notice.controller;

import com._1000meal.notice.dto.NoticeCreateRequest;
import com._1000meal.notice.dto.NoticeResponse;
import com._1000meal.notice.dto.NoticeUpdateRequest;
import com._1000meal.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public List<NoticeResponse> list() {
        return noticeService.list();
    }

    @GetMapping("/{id}")
    public NoticeResponse get(@PathVariable Long id) {
        return noticeService.get(id);
    }

    @PostMapping
    public NoticeResponse create(@RequestBody @Valid NoticeCreateRequest request) {
        return noticeService.create(request);
    }

    @PutMapping("/{id}")
    public NoticeResponse update(@PathVariable Long id, @RequestBody @Valid NoticeUpdateRequest request) {
        return noticeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        noticeService.delete(id);
    }
}

