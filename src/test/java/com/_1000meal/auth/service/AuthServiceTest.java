package com._1000meal.auth.service;

import com._1000meal.auth.dto.LoginRequest;
import com._1000meal.auth.dto.LoginResponse;
import com._1000meal.auth.dto.SignupRequest;
import com._1000meal.auth.dto.SignupResponse;
import com._1000meal.auth.model.*;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.email.service.EmailService;
import com._1000meal.global.constant.Role;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.security.JwtProvider;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String VALID_PW = "Str0ng!Pw9A";

    @Mock AccountRepository accountRepo;
    @Mock UserProfileRepository userProfileRepo;
    @Mock AdminProfileRepository adminProfileRepo;
    @Mock StoreRepository storeRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;
    @Mock EmailService emailService;

    @InjectMocks AuthService authService;

    @Test
    @DisplayName("회원가입(STUDENT): 이메일 인증 선행(requireVerified) + 계정 저장 + 프로필 저장 + 인증토큰 소진")
    void signup_student_success() {
        // given
        SignupRequest req = new SignupRequest(
                Role.STUDENT,          // role
                "20250001",            // userId
                "주흔",                // name
                "test@sch.ac.kr",      // email
                VALID_PW,      // password
                null                  // storeId (학생이면 null)
        );

        when(accountRepo.existsByUserIdAndStatusNot("20250001", AccountStatus.DELETED)).thenReturn(false);
        when(accountRepo.existsByEmailAndStatusNot("test@sch.ac.kr", AccountStatus.DELETED)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("ENC");

        // save 시 id 세팅(서비스에서 account.getId()를 쓰므로)
        doAnswer(inv -> {
            Account a = inv.getArgument(0);
            setField(a, "id", 1L);
            return a;
        }).when(accountRepo).save(any(Account.class));

        // when
        SignupResponse res = authService.signup(req);

        // then
        verify(emailService).requireVerified("test@sch.ac.kr");
        verify(accountRepo).save(any(Account.class));
        verify(userProfileRepo).save(any(UserProfile.class));
        verify(emailService).consumeAllFor("test@sch.ac.kr");

        assertEquals(1L, res.accountId());
        assertEquals(Role.STUDENT, res.role());
        assertEquals("20250001", res.userId());
        assertEquals("test@sch.ac.kr", res.email());
    }

    @Test
    @DisplayName("회원가입(STUDENT): 이메일 인증이 안 되어 있으면 실패")
    void signup_student_requiresVerified() {
        SignupRequest req = new SignupRequest(
                Role.STUDENT,
                "20250001",
                "주흔",
                "test@sch.ac.kr",
                VALID_PW,
                null
        );

        // 여기만 필요
        doThrow(new CustomException(com._1000meal.global.error.code.ErrorCode.VALIDATION_ERROR, "인증 필요"))
                .when(emailService).requireVerified("test@sch.ac.kr");

        assertThrows(CustomException.class, () -> authService.signup(req));

        // verify: 인증 실패가 “첫 관문”이라면 repo/save가 호출되지 않는 게 정상
        verify(emailService).requireVerified("test@sch.ac.kr");
        verifyNoInteractions(accountRepo);
        verifyNoInteractions(userProfileRepo);
        verify(emailService, never()).consumeAllFor(anyString());
    }

    @Test
    @DisplayName("로그인: 비밀번호 일치 + 역할 일치 + ACTIVE 이면 JWT 발급")
    void login_success() {
        LoginRequest req = new LoginRequest(
                Role.STUDENT,     // role
                "20250001",       // userId
                "pw"              // password
        );

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(10L);
        when(account.getUserId()).thenReturn("20250001");
        when(account.getEmail()).thenReturn("test@sch.ac.kr");
        when(account.getRole()).thenReturn(Role.STUDENT);
        when(account.getStatus()).thenReturn(AccountStatus.ACTIVE);
        when(account.getPasswordHash()).thenReturn("HASH");

        when(accountRepo.findByUserId("20250001")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);

        // 표시 이름은 userProfileRepo에서 가져오므로 없으면 null 가능
        when(userProfileRepo.findByAccountId(10L)).thenReturn(Optional.empty());
        when(jwtProvider.createToken(any(AuthPrincipal.class), any())).thenReturn("ACCESS_TOKEN");

        LoginResponse res = authService.login(req);

        assertEquals("ACCESS_TOKEN", res.accessToken());
        assertEquals(10L, res.accountId());
        verify(jwtProvider).createToken(any(AuthPrincipal.class), isNull());
    }

    @Test
    @DisplayName("로그인: 역할 불일치면 ROLE_MISMATCH")
    void login_roleMismatch() {
        LoginRequest req = new LoginRequest(
                Role.STUDENT,     // role
                "20250001",       // userId
                "pw"              // password
        );

        Account account = mock(Account.class);
        when(account.getPasswordHash()).thenReturn("HASH");
        when(account.getRole()).thenReturn(Role.STUDENT);

        when(accountRepo.findByUserId("20250001")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.login(req));
        verify(jwtProvider, never()).createToken(any(), any());
    }

    @Test
    @DisplayName("내 정보(me): Authentication principal.id로 계정 조회 후 응답")
    void me_success() {
        Authentication authentication = mock(Authentication.class);
        AuthPrincipal principal = new AuthPrincipal(10L, "20250001", "주흔", "test@sch.ac.kr", "STUDENT");

        when(authentication.getPrincipal()).thenReturn(principal);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(10L);
        when(account.getRole()).thenReturn(Role.STUDENT);
        when(account.getUserId()).thenReturn("20250001");
        when(account.getEmail()).thenReturn("test@sch.ac.kr");

        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));

        LoginResponse res = authService.me(authentication);

        assertEquals(10L, res.accountId());
        assertEquals("20250001", res.userId());
        assertEquals("test@sch.ac.kr", res.email());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }
}