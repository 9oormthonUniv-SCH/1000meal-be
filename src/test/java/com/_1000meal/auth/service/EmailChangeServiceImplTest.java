package com._1000meal.auth.service;

import com._1000meal.auth.dto.ChangeEmailConfirmRequest;
import com._1000meal.auth.dto.EmailChange.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailChangeServiceImplTest {

    @Mock AccountService accountService;
    @InjectMocks EmailChangeServiceImpl service;

    @Test
    @DisplayName("start: 현재 이메일/비번 검증 후 changeId 발급")
    void start_success() {
        EmailChangeStartResponse res = service.start(1L, new EmailChangeStartRequest("old@sch.ac.kr", "pw"));
        assertNotNull(res.changeId());
        assertTrue(res.expiresInSec() > 0);

        verify(accountService).verifyCredentialForEmailChange(1L, "old@sch.ac.kr", "pw");
    }

    @Test
    @DisplayName("requestCode: changeId 유효 + accountId 일치 시 새 이메일로 코드 발송")
    void requestCode_success() {
        String changeId = service.start(1L, new EmailChangeStartRequest("old@sch.ac.kr", "pw")).changeId();

        service.requestCode(1L, new EmailChangeRequestCodeRequest(changeId, "new@sch.ac.kr"));

        verify(accountService).requestChangeEmailCode(1L, "new@sch.ac.kr");
    }

    @Test
    @DisplayName("verify: 코드 검증 후 confirmChangeEmail 호출, 완료 후 티켓 제거")
    void verify_success() {
        String changeId = service.start(1L, new EmailChangeStartRequest("old@sch.ac.kr", "pw")).changeId();
        service.requestCode(1L, new EmailChangeRequestCodeRequest(changeId, "new@sch.ac.kr"));

        EmailChangeVerifyResponse res = service.verify(1L, new EmailChangeVerifyRequest(changeId, "123456"));

        verify(accountService).confirmChangeEmail(1L, new ChangeEmailConfirmRequest("new@sch.ac.kr", "123456"));
        assertEquals("new@sch.ac.kr", res.updatedEmail());
    }
}