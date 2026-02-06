package com._1000meal.auth.service;

import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentAccountProvider {

    private final AdminProfileRepository adminProfileRepository;

    public Long getCurrentAccountId() {
        return getPrincipal().id();
    }

    public Long getCurrentStoreId() {
        AuthPrincipal principal = getPrincipal();
        if (!"ADMIN".equals(principal.role())) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }
        return adminProfileRepository.findByAccountId(principal.id())
                .map(profile -> profile.getStore() != null ? profile.getStore().getId() : null)
                .orElseThrow(() -> {
                    // TODO(2026-02-05): 안정화 후 제거 - STORE_403 원인 추적 로그
                    log.debug("[STORE_403][ADMIN_PROFILE_MISSING] accountId={}", principal.id());
                    return new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
                });
    }

    private AuthPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }
}
