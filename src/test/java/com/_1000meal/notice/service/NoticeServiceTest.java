package com._1000meal.notice.service;

import com._1000meal.global.error.code.NoticeErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.notice.domain.Notice;
import com._1000meal.notice.dto.NoticeCreateRequest;
import com._1000meal.notice.dto.NoticeResponse;
import com._1000meal.notice.dto.NoticeUpdateRequest;
import com._1000meal.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService;



    @Test
    @DisplayName("list: deletedAt null인 공지를 isPinned desc, createdAt desc로 조회하고 Response로 변환한다")
    void list_success_sortedAndMapped() {
        // given
        Notice pinned = spy(Notice.builder()
                .title("공지1")
                .content("내용1")
                .isPublished(true)
                .isPinned(true)
                .build());

        Notice normal = spy(Notice.builder()
                .title("공지2")
                .content("내용2")
                .isPublished(true)
                .isPinned(false)
                .build());

        // ✅ toResponse 내부에서 createdAt/updatedAt format() 호출 -> 둘 다 null 방지
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0)).when(pinned).getCreatedAt();
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0)).when(pinned).getUpdatedAt();

        doReturn(LocalDateTime.of(2026, 1, 1, 9, 0)).when(normal).getCreatedAt();
        doReturn(LocalDateTime.of(2026, 1, 1, 9, 0)).when(normal).getUpdatedAt();

        when(noticeRepository.findAllByDeletedAtIsNull(any(Sort.class)))
                .thenReturn(List.of(pinned, normal));

        // when
        var result = noticeService.getAllNotice();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(noticeRepository).findAllByDeletedAtIsNull(sortCaptor.capture());

        List<Sort.Order> orders = sortCaptor.getValue().toList();
        assertEquals(2, orders.size());

        assertEquals("isPinned", orders.get(0).getProperty());
        assertTrue(orders.get(0).isDescending());

        assertEquals("createdAt", orders.get(1).getProperty());
        assertTrue(orders.get(1).isDescending());

        verifyNoMoreInteractions(noticeRepository);
    }

    @Test
    @DisplayName("get: 없으면 NOTICE_NOT_FOUND 예외")
    void get_notFound() {
        // given
        when(noticeRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException ex = assertThrows(CustomException.class, () -> noticeService.getNotice(1L));

        // then
        assertEquals(NoticeErrorCode.NOTICE_NOT_FOUND, ex.getErrorCodeIfs());
        verify(noticeRepository).findById(1L);
        verifyNoMoreInteractions(noticeRepository);
    }

    @Test
    @DisplayName("create: 요청으로 Notice 생성 후 save하고 NoticeResponse 반환")
    void create_success() {
        // given
        NoticeCreateRequest req = new NoticeCreateRequest(
                "제목",
                "내용",
                true,
                false
        );

        when(noticeRepository.save(any(Notice.class)))
                .thenAnswer(invocation -> {
                    Notice n = invocation.getArgument(0);

                    // ✅ toResponse()에서 format() 호출하므로 createdAt/updatedAt 세팅 필요
                    setField(n, "createdAt", LocalDateTime.of(2026, 1, 1, 10, 0));
                    setField(n, "updatedAt", LocalDateTime.of(2026, 1, 1, 10, 0));

                    // id도 응답에 필요하면 같이 세팅(필드명이 다르면 수정)
                    // setField(n, "id", 1L);

                    return n;
                });

        // when
        NoticeResponse resp = noticeService.create(req,null);

        // then
        assertNotNull(resp);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        Notice saved = captor.getValue();
        assertNotNull(saved);

        // 여기서 Notice에 getter가 있으면 검증 강화 가능
        // assertEquals("제목", saved.getTitle());
        // assertEquals("내용", saved.getContent());

        verifyNoMoreInteractions(noticeRepository);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (NoSuchFieldException e) {
            // 상속 구조(BaseEntity)일 수 있으니 부모까지 탐색
            Class<?> c = target.getClass().getSuperclass();
            while (c != null) {
                try {
                    Field f = c.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException ignore) {
                    c = c.getSuperclass();
                } catch (IllegalAccessException iae) {
                    throw new RuntimeException(iae);
                }
            }
            throw new RuntimeException("필드가 없습니다: " + fieldName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @DisplayName("update: 존재하면 엔티티 update 호출 후 NoticeResponse 반환")
    void update_success_callsEntityUpdate() {
        // given
        Notice n = mock(Notice.class);

        // ✅ toResponse()에서 format()하므로 createdAt/updatedAt은 반드시 non-null
        when(n.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(n.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 0));

        // id/title/content/isPublished/isPinned 등 toResponse()가 참조하면 같이 스텁
        // (필요한 것만 남겨도 됨)
        when(n.getId()).thenReturn(1L);
        when(n.getTitle()).thenReturn("기존제목");
        when(n.getContent()).thenReturn("기존내용");
        when(n.isPublished()).thenReturn(true);
        when(n.isPinned()).thenReturn(false);

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(n));

        NoticeUpdateRequest req = new NoticeUpdateRequest(
                "수정제목",
                "수정내용",
                false,
                true
        );

        // when
        NoticeResponse resp = noticeService.update(1L, req);

        // then
        assertNotNull(resp);
        verify(noticeRepository).findById(1L);
        verify(n).update("수정제목", "수정내용", false, true);
        verifyNoMoreInteractions(noticeRepository);
    }

    @Test
    @DisplayName("update: 없으면 NOTICE_NOT_FOUND 예외")
    void update_notFound() {
        // given
        when(noticeRepository.findById(1L)).thenReturn(Optional.empty());
        NoticeUpdateRequest req = new NoticeUpdateRequest("t", "c", true, false);

        // when
        CustomException ex = assertThrows(CustomException.class, () -> noticeService.update(1L, req));

        // then
        assertEquals(NoticeErrorCode.NOTICE_NOT_FOUND, ex.getErrorCodeIfs());
        verify(noticeRepository).findById(1L);
        verifyNoMoreInteractions(noticeRepository);
    }

    @Test
    @DisplayName("delete: 존재하면 엔티티 softDelete 호출")
    void delete_success_callsSoftDelete() {
        // given
        Notice n = mock(Notice.class);
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(n));

        // when
        noticeService.delete(1L);

        // then
        verify(noticeRepository).findById(1L);
        verify(n).softDelete();
        verifyNoMoreInteractions(noticeRepository);
    }

    @Test
    @DisplayName("delete: 없으면 NOTICE_NOT_FOUND 예외")
    void delete_notFound() {
        // given
        when(noticeRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException ex = assertThrows(CustomException.class, () -> noticeService.delete(1L));

        // then
        assertEquals(NoticeErrorCode.NOTICE_NOT_FOUND, ex.getErrorCodeIfs());
        verify(noticeRepository).findById(1L);
        verifyNoMoreInteractions(noticeRepository);
    }
}
