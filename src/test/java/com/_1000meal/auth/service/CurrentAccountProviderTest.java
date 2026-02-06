package com._1000meal.auth.service;

import com._1000meal.auth.model.AdminProfile;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.store.domain.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentAccountProviderTest {

    @Mock AdminProfileRepository adminProfileRepository;
    @InjectMocks CurrentAccountProvider provider;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("admin_profiles 기반으로 storeId 조회")
    void getCurrentStoreId_usesAdminProfile() {
        AuthPrincipal principal = new AuthPrincipal(18L, "admin18", "관리자", null, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null)
        );

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(1L);

        AdminProfile profile = mock(AdminProfile.class);
        when(profile.getStore()).thenReturn(store);
        when(adminProfileRepository.findByAccountId(18L)).thenReturn(Optional.of(profile));

        Long storeId = provider.getCurrentStoreId();

        assertEquals(1L, storeId);
        verify(adminProfileRepository).findByAccountId(18L);
    }

    @Test
    @DisplayName("admin_profiles 미존재 시 STORE_403")
    void getCurrentStoreId_noProfileThrows() {
        AuthPrincipal principal = new AuthPrincipal(18L, "admin18", "관리자", null, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null)
        );

        when(adminProfileRepository.findByAccountId(18L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, provider::getCurrentStoreId);
        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
    }
}
