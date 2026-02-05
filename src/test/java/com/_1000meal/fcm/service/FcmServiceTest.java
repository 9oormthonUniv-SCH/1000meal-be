package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.FcmPlatform;
import com._1000meal.fcm.domain.FcmToken;
import com._1000meal.fcm.domain.NotificationPreference;
import com._1000meal.fcm.repository.FcmTokenRepository;
import com._1000meal.fcm.repository.NotificationPreferenceRepository;
import com._1000meal.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @Mock FcmTokenRepository tokenRepository;
    @Mock NotificationPreferenceRepository preferenceRepository;

    @InjectMocks FcmService service;

    @Test
    @DisplayName("registerOrRelinkToken: token 빈값이면 VALIDATION_ERROR")
    void registerToken_blank_throws() {
        assertThrows(CustomException.class,
                () -> service.registerOrRelinkToken(1L, "  ", FcmPlatform.WEB));

        verifyNoInteractions(tokenRepository, preferenceRepository);
    }

    @Test
    @DisplayName("registerOrRelinkToken: 신규 토큰이면 저장 + preference 기본ON 생성")
    void registerToken_new_savesAndCreatePref() {
        when(tokenRepository.findByToken("t1")).thenReturn(Optional.empty());
        when(preferenceRepository.findByAccountId(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.registerOrRelinkToken(1L, "t1", FcmPlatform.WEB);

        verify(tokenRepository).findByToken("t1");

        ArgumentCaptor<FcmToken> tokenCaptor = ArgumentCaptor.forClass(FcmToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals(1L, tokenCaptor.getValue().getAccountId());
        assertEquals("t1", tokenCaptor.getValue().getToken());
        assertEquals(FcmPlatform.WEB, tokenCaptor.getValue().getPlatform());

        verify(preferenceRepository).findByAccountId(1L);
        verify(preferenceRepository).save(any(NotificationPreference.class));

        verifyNoMoreInteractions(tokenRepository, preferenceRepository);
    }

    @Test
    @DisplayName("registerOrRelinkToken: 기존 토큰이면 relink만 하고 신규 save는 안 함")
    void registerToken_existing_relink() {
        FcmToken existing = mock(FcmToken.class);
        when(tokenRepository.findByToken("t1")).thenReturn(Optional.of(existing));
        when(preferenceRepository.findByAccountId(1L)).thenReturn(Optional.of(mock(NotificationPreference.class)));

        service.registerOrRelinkToken(1L, "t1", FcmPlatform.ANDROID);

        verify(existing).relink(1L, FcmPlatform.ANDROID);
        verify(tokenRepository, never()).save(any());
        verify(preferenceRepository, never()).save(any());

        verifyNoMoreInteractions(tokenRepository, preferenceRepository);
    }

    @Test
    @DisplayName("setEnabled: preference 없으면 생성 후 enabled 변경")
    void setEnabled_createIfMissing() {
        when(preferenceRepository.findByAccountId(1L)).thenReturn(Optional.empty());
        NotificationPreference saved = NotificationPreference.createDefaultOn(1L);
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(saved);

        service.setEnabled(1L, false);

        assertFalse(saved.isEnabled());

        verify(preferenceRepository).findByAccountId(1L);
        verify(preferenceRepository).save(any(NotificationPreference.class));
        verifyNoMoreInteractions(preferenceRepository);
        verifyNoInteractions(tokenRepository);
    }
}