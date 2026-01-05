package com._1000meal.auth.service;

import com._1000meal.auth.dto.ChangeEmailConfirmRequest;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.email.service.EmailService;
import com._1000meal.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    @InjectMocks AccountService accountService;

    @Test
    @DisplayName("verifyCredentialForEmailChange: 현재 이메일 불일치면 실패")
    void verifyCredential_emailMismatch() {
        Account account = mock(Account.class);
        when(account.getEmail()).thenReturn("old@sch.ac.kr");

        when(accountRepository.findByIdAndStatusNot(1L, AccountStatus.DELETED)).thenReturn(Optional.of(account));

        assertThrows(CustomException.class,
                () -> accountService.verifyCredentialForEmailChange(1L, "different@sch.ac.kr", "pw"));
    }

    @Test
    @DisplayName("requestChangeEmailCode: 학교 도메인 아니면 실패")
    void requestChangeEmailCode_rejectNonSchoolDomain() {
        Account account = mock(Account.class);
        //when(account.getEmail()).thenReturn("old@sch.ac.kr");

        when(accountRepository.findByIdAndStatusNot(1L, AccountStatus.DELETED)).thenReturn(Optional.of(account));

        assertThrows(CustomException.class,
                () -> accountService.requestChangeEmailCode(1L, "new@gmail.com"));

        verify(emailService, never()).issueAndStoreCode(anyString());
    }

    @Test
    @DisplayName("confirmChangeEmail: 코드 검증 후 consumeAllFor + account.changeEmail 호출")
    void confirmChangeEmail_successFlow() {
        Account account = mock(Account.class);

        when(accountRepository.findByIdAndStatusNot(1L, AccountStatus.DELETED)).thenReturn(Optional.of(account));
        when(accountRepository.existsByEmailAndStatusNot("new@sch.ac.kr", AccountStatus.DELETED)).thenReturn(false);

        ChangeEmailConfirmRequest req = new ChangeEmailConfirmRequest("new@sch.ac.kr", "123456");

        accountService.confirmChangeEmail(1L, req);

        verify(emailService).verifyCode("new@sch.ac.kr", "123456");
        verify(emailService).consumeAllFor("new@sch.ac.kr");
        verify(account).changeEmail("new@sch.ac.kr");
    }
}