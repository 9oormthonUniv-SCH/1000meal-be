package com._1000meal.global.security;

import com._1000meal.auth.model.AuthPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtProvider jwtProvider;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ADMIN 토큰 인증 시 principal.accountId 세팅")
    void filter_setsAccountIdFromClaims() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test.token");

        Claims claims = Jwts.claims();
        claims.put("id", 18L);
        claims.put("account", "admin18");
        claims.put("role", "ADMIN");
        claims.put("name", "관리자");
        claims.put("email", "admin18@sch.ac.kr");

        when(jwtProvider.validate("test.token")).thenReturn(true);
        when(jwtProvider.parse("test.token")).thenReturn(claims);

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(auth.getPrincipal() instanceof AuthPrincipal);
        AuthPrincipal principal = (AuthPrincipal) auth.getPrincipal();
        assertEquals(18L, principal.id());
        assertEquals("ADMIN", principal.role());
    }
}
