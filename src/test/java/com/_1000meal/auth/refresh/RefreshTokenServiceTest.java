package com._1000meal.auth.refresh;

import com._1000meal.auth.dto.RefreshTokenResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.global.constant.Role;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtProvider jwtProvider;
    @Mock UserProfileRepository userProfileRepository;
    @Mock AdminProfileRepository adminProfileRepository;

    @InjectMocks RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("refresh_success_issues_new_access")
    void refresh_success_issues_new_access() {
        String rawRefresh = "raw.refresh.token";
        String tokenHash = "a".repeat(64);

        Account account = mockAccount(10L, Role.STUDENT, AccountStatus.ACTIVE);
        RefreshToken entity = RefreshToken.issue(
                account,
                tokenHash,
                LocalDateTime.now().plusDays(1),
                null,
                null,
                null
        );

        Claims claims = Jwts.claims();
        claims.setSubject("10");
        claims.put("type", "refresh");

        when(jwtProvider.parseAndValidateRefreshToken(rawRefresh)).thenReturn(claims);
        when(jwtProvider.sha256Hex(rawRefresh)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(entity));
        when(jwtProvider.createToken(any(), any())).thenReturn("new.access.token");
        when(jwtProvider.getAccessExpSeconds()).thenReturn(1800L);
        when(userProfileRepository.findByAccountId(10L)).thenReturn(Optional.empty());

        RefreshTokenResponse response = refreshTokenService.refresh(rawRefresh);

        assertEquals("new.access.token", response.accessToken());
        assertEquals(1800L, response.expiresInSeconds());
        assertNotNull(entity.getLastUsedAt());
    }

    @Test
    @DisplayName("refresh_fail_when_token_not_in_db")
    void refresh_fail_when_token_not_in_db() {
        String rawRefresh = "raw.refresh.token";
        String tokenHash = "b".repeat(64);
        Claims claims = Jwts.claims();
        claims.setSubject("10");
        claims.put("type", "refresh");

        when(jwtProvider.parseAndValidateRefreshToken(rawRefresh)).thenReturn(claims);
        when(jwtProvider.sha256Hex(rawRefresh)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> refreshTokenService.refresh(rawRefresh));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("refresh_fail_when_revoked")
    void refresh_fail_when_revoked() {
        String rawRefresh = "raw.refresh.token";
        String tokenHash = "c".repeat(64);

        Account account = mockAccount(10L, Role.STUDENT, AccountStatus.ACTIVE);
        RefreshToken entity = RefreshToken.issue(
                account,
                tokenHash,
                LocalDateTime.now().plusDays(1),
                null,
                null,
                null
        );
        entity.revoke(LocalDateTime.now());

        Claims claims = Jwts.claims();
        claims.setSubject("10");
        claims.put("type", "refresh");

        when(jwtProvider.parseAndValidateRefreshToken(rawRefresh)).thenReturn(claims);
        when(jwtProvider.sha256Hex(rawRefresh)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(entity));

        CustomException ex = assertThrows(CustomException.class, () -> refreshTokenService.refresh(rawRefresh));
        assertEquals(ErrorCode.REVOKED_REFRESH_TOKEN, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("logout_revokes_token")
    void logout_revokes_token() {
        String rawRefresh = "raw.refresh.token";
        String tokenHash = "d".repeat(64);

        Account account = mockAccount(10L, Role.STUDENT, AccountStatus.ACTIVE);
        RefreshToken entity = RefreshToken.issue(
                account,
                tokenHash,
                LocalDateTime.now().plusDays(1),
                null,
                null,
                null
        );

        when(jwtProvider.sha256Hex(rawRefresh)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(entity));

        refreshTokenService.logout(rawRefresh);

        assertTrue(entity.isRevoked());
        verify(refreshTokenRepository).findByTokenHash(tokenHash);
    }

    private Account mockAccount(Long id, Role role, AccountStatus status) {
        Account account = org.mockito.Mockito.mock(Account.class);
        when(account.getId()).thenReturn(id);
        when(account.getRole()).thenReturn(role);
        when(account.getStatus()).thenReturn(status);
        when(account.getUserId()).thenReturn("20250001");
        when(account.getEmail()).thenReturn("test@sch.ac.kr");
        return account;
    }
}
