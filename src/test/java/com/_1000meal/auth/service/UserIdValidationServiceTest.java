package com._1000meal.auth.service;

import com._1000meal.auth.dto.UserIdValidateResponse;
import com._1000meal.auth.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIdValidationServiceTest {

    @Mock AccountRepository accountRepository;
    @InjectMocks UserIdValidationService service;

    @Test
    @DisplayName("학번 형식이 아니면 INVALID_FORMAT + valid=false")
    void invalidFormat() {
        UserIdValidateResponse res = service.validate("abc");

        assertFalse(res.isValid());
        assertEquals("INVALID_FORMAT", res.getStatus());
        assertNotNull(res.getMessage());

        verifyNoInteractions(accountRepository);
    }

    @Test
    @DisplayName("이미 사용 중이면 TAKEN + valid=false")
    void taken() {
        when(accountRepository.existsByUserId("20250001")).thenReturn(true);

        UserIdValidateResponse res = service.validate(" 20250001 ");

        assertFalse(res.isValid());
        assertEquals("DUPLICATED", res.getStatus());
        assertNotNull(res.getMessage());
    }

    @Test
    @DisplayName("사용 가능하면 AVAILABLE + valid=true")
    void available() {
        when(accountRepository.existsByUserId("20250001")).thenReturn(false);

        UserIdValidateResponse res = service.validate("20250001");

        assertTrue(res.isValid());
        assertEquals("AVAILABLE", res.getStatus());
        assertNotNull(res.getMessage());
    }
}