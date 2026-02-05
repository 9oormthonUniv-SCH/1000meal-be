package com._1000meal.email.service;

import com._1000meal.email.domain.EmailVerificationToken;
import com._1000meal.email.repository.EmailVerificationTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock EmailVerificationTokenRepository tokenRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks EmailService emailService;

    @Test
    @DisplayName("학교 도메인이 아니면 인증 코드 발급이 실패한다")
    void issueAndStoreCode_rejectsNonSchoolDomain() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.issueAndStoreCode("test@gmail.com"));

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("인증 코드 발급 시: 기존 미검증 토큰 삭제 -> 새 토큰 저장 -> 커밋 후 메일 이벤트 발행")
    void issueAndStoreCode_flow() {
        // generateCode()가 랜덤이라 테스트 안정성을 위해 spy로 고정
        EmailService spy = Mockito.spy(emailService);
        doReturn("123456").when(spy).generateCode();

        spy.issueAndStoreCode("  USER@sch.ac.kr ");

        verify(tokenRepository).deleteByEmailAndVerifiedFalse("user@sch.ac.kr");

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        // EmailVerificationToken에 getEmail()이 없다면 아래 2줄은 프로젝트에 맞게 조정
        assertEquals("user@sch.ac.kr", tokenCaptor.getValue().getEmail());
        assertEquals("123456", tokenCaptor.getValue().getCode());

        ArgumentCaptor<EmailService.VerificationMailRequested> eventCaptor =
                ArgumentCaptor.forClass(EmailService.VerificationMailRequested.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        assertEquals("user@sch.ac.kr", eventCaptor.getValue().email());
        assertEquals("123456", eventCaptor.getValue().code());
    }

    @Test
    @DisplayName("verifyCode: 유효한 인증 요청이 없으면 예외")
    void verifyCode_noRequest() {
        when(tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc("a@sch.ac.kr"))
                .thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> emailService.verifyCode("a@sch.ac.kr", "123456"));

        assertTrue(ex.getMessage().contains("유효한 인증 요청"));
    }

    @Test
    @DisplayName("verifyCode: 만료된 토큰이면 예외")
    void verifyCode_expired() {
        EmailVerificationToken token = mock(EmailVerificationToken.class);
        when(token.isExpired()).thenReturn(true);

        when(tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc("a@sch.ac.kr"))
                .thenReturn(Optional.of(token));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> emailService.verifyCode("a@sch.ac.kr", "123456"));

        assertTrue(ex.getMessage().contains("만료"));
        verify(token, never()).markVerified();
    }

    @Test
    @DisplayName("verifyCode: 코드 불일치면 예외")
    void verifyCode_mismatch() {
        EmailVerificationToken token = mock(EmailVerificationToken.class);
        when(token.isExpired()).thenReturn(false);
        when(token.getCode()).thenReturn("111111");

        when(tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc("a@sch.ac.kr"))
                .thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class,
                () -> emailService.verifyCode("a@sch.ac.kr", "222222"));

        verify(token, never()).markVerified();
    }

    @Test
    @DisplayName("verifyCode: 성공 시 markVerified() 호출")
    void verifyCode_success() {
        EmailVerificationToken token = mock(EmailVerificationToken.class);
        when(token.isExpired()).thenReturn(false);
        when(token.getCode()).thenReturn("123456");

        when(tokenRepository.findTop1ByEmailAndVerifiedFalseOrderByIdDesc("a@sch.ac.kr"))
                .thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> emailService.verifyCode("a@sch.ac.kr", "123456"));
        verify(token).markVerified();
    }

    @Test
    @DisplayName("requireVerified: 인증 완료 토큰이 없으면 예외")
    void requireVerified_notVerified() {
        when(tokenRepository.findTop1ByEmailAndVerifiedTrueOrderByIdDesc("a@sch.ac.kr"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> emailService.requireVerified("a@sch.ac.kr"));
    }

    @Test
    @DisplayName("requireVerified: 인증 완료 토큰이 만료되면 예외")
    void requireVerified_expired() {
        EmailVerificationToken token = mock(EmailVerificationToken.class);
        when(token.isExpired()).thenReturn(true);

        when(tokenRepository.findTop1ByEmailAndVerifiedTrueOrderByIdDesc("a@sch.ac.kr"))
                .thenReturn(Optional.of(token));

        assertThrows(IllegalStateException.class, () -> emailService.requireVerified("a@sch.ac.kr"));
    }

    @Test
    @DisplayName("isEmailVerified: repository existsBy...로 위임한다")
    void isEmailVerified_delegates() {
        when(tokenRepository.existsByEmailAndVerifiedTrue("a@sch.ac.kr")).thenReturn(true);

        assertTrue(emailService.isEmailVerified(" a@sch.ac.kr "));
        verify(tokenRepository).existsByEmailAndVerifiedTrue("a@sch.ac.kr");
    }
}