package com._1000meal.adminlogin.service;

import com._1000meal.adminlogin.dto.AdminSignupRequest;
import com._1000meal.adminlogin.dto.PasswordChangeRequest;
import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.adminlogin.repository.AdminRepository;
import com._1000meal.global.error.code.AdminSignupErrorCode;
import com._1000meal.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private AdminSignupRequest mockSignupReq(String username, String password, String name, String phone) {
        AdminSignupRequest req = mock(AdminSignupRequest.class);
        when(req.getUsername()).thenReturn(username);
        when(req.getPassword()).thenReturn(password);
        when(req.getName()).thenReturn(name);
        when(req.getPhoneNumber()).thenReturn(phone);
        return req;
    }

    @Test
    @DisplayName("signup: username 중복이면 ADMIN_ALREADY_EXISTS")
    void signup_duplicateUsername_throws() {
        // given: 중복 체크에서 username만 쓰이므로 username만 스텁
        AdminSignupRequest req = mock(AdminSignupRequest.class);
        when(req.getUsername()).thenReturn("admin");

        when(adminRepository.findByUsername("admin"))
                .thenReturn(Optional.of(mock(AdminEntity.class)));

        // when
        CustomException ex = assertThrows(CustomException.class, () -> adminService.signup(req));

        // then
        assertEquals(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS, ex.getErrorCodeIfs());
        verify(adminRepository).findByUsername("admin");
        verifyNoMoreInteractions(adminRepository);
        verifyNoInteractions(passwordEncoder);
    }



    @Test
    @DisplayName("signup: 필수값 누락이면 REQUIRED_FIELD_MISSING")
    void signup_requiredMissing_throws() {
        // given: username만 null로 두면 1차 중복체크 -> 2차 필수값 체크로 진입
        AdminSignupRequest req = mock(AdminSignupRequest.class);
        when(req.getUsername()).thenReturn(null);

        // 서비스가 실제로 findByUsername(null)을 호출함
        when(adminRepository.findByUsername(isNull())).thenReturn(Optional.empty());

        // when
        CustomException ex = assertThrows(CustomException.class, () -> adminService.signup(req));

        // then
        assertEquals(AdminSignupErrorCode.REQUIRED_FIELD_MISSING, ex.getErrorCodeIfs());
        verify(adminRepository).findByUsername(isNull());
        verifyNoMoreInteractions(adminRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("signup: 비밀번호 길이 < 8이면 PASSWORD_WEAK")
    void signup_passwordTooShort_throws() {
        // given
        AdminSignupRequest req = mock(AdminSignupRequest.class);
        when(req.getUsername()).thenReturn("admin");
        when(req.getPassword()).thenReturn("1234567");
        when(req.getName()).thenReturn("관리자"); // null 체크 통과용

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());

        // when
        CustomException ex = assertThrows(CustomException.class, () -> adminService.signup(req));

        // then
        assertEquals(AdminSignupErrorCode.PASSWORD_WEAK, ex.getErrorCodeIfs());
        verify(adminRepository).findByUsername("admin");
        verifyNoMoreInteractions(adminRepository);
        verifyNoInteractions(passwordEncoder);
    }



    @Test
    @DisplayName("signup: 정상 요청이면 encode 후 save한다")
    void signup_success_encodesAndSaves() {
        // PasswordValidator 정책에 걸리지 않도록 (username/phone 포함 X, 길이 충분, 조합 포함) 값 사용
        AdminSignupRequest req = mockSignupReq("admin", "Str0ng!Passw0rd", "관리자", "01012345678");

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Str0ng!Passw0rd")).thenReturn("ENCODED");
        when(adminRepository.save(any(AdminEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminEntity saved = adminService.signup(req);

        assertNotNull(saved);

        ArgumentCaptor<AdminEntity> captor = ArgumentCaptor.forClass(AdminEntity.class);
        verify(adminRepository).save(captor.capture());
        AdminEntity entity = captor.getValue();
        assertNotNull(entity);
        assertEquals("admin", entity.getUsername());
        assertEquals("ENCODED", entity.getPassword());
        assertEquals("관리자", entity.getName());
        assertEquals("01012345678", entity.getPhoneNumber());

        verify(adminRepository).findByUsername("admin");
        verify(passwordEncoder).encode("Str0ng!Passw0rd");
        verifyNoMoreInteractions(adminRepository, passwordEncoder);
    }

    @Test
    @DisplayName("authenticate: username 없으면 예외")
    void authenticate_notFound_throws() {
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> adminService.authenticate("admin", "raw"));
        assertEquals(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS, ex.getErrorCodeIfs());

        verify(adminRepository).findByUsername("admin");
        verifyNoMoreInteractions(adminRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("authenticate: 비밀번호 불일치면 PASSWORD_MISMATCH")
    void authenticate_passwordMismatch_throws() {
        AdminEntity admin = mock(AdminEntity.class);
        when(admin.getPassword()).thenReturn("ENCODED");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("raw", "ENCODED")).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class,
                () -> adminService.authenticate("admin", "raw"));
        assertEquals(AdminSignupErrorCode.PASSWORD_MISMATCH, ex.getErrorCodeIfs());

        verify(adminRepository).findByUsername("admin");
        verify(passwordEncoder).matches("raw", "ENCODED");
        verifyNoMoreInteractions(adminRepository, passwordEncoder);
    }

    @Test
    @DisplayName("authenticate: 비밀번호 일치면 admin 반환")
    void authenticate_success_returnsAdmin() {
        AdminEntity admin = mock(AdminEntity.class);
        when(admin.getPassword()).thenReturn("ENCODED");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("raw", "ENCODED")).thenReturn(true);

        AdminEntity result = adminService.authenticate("admin", "raw");

        assertSame(admin, result);
        verify(adminRepository).findByUsername("admin");
        verify(passwordEncoder).matches("raw", "ENCODED");
        verifyNoMoreInteractions(adminRepository, passwordEncoder);
    }

    @Test
    @DisplayName("getAdminByUsername: 존재하면 반환")
    void getAdminByUsername_success() {
        AdminEntity admin = mock(AdminEntity.class);
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        AdminEntity result = adminService.getAdminByUsername("admin");

        assertSame(admin, result);
        verify(adminRepository).findByUsername("admin");
        verifyNoMoreInteractions(adminRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("getAdminByUsername: 없으면 예외")
    void getAdminByUsername_notFound_throws() {
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> adminService.getAdminByUsername("admin"));
        assertEquals(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS, ex.getErrorCodeIfs());

        verify(adminRepository).findByUsername("admin");
        verifyNoMoreInteractions(adminRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("changePassword: oldPassword 불일치면 PASSWORD_MISMATCH, 저장 안 함")
    void changePassword_oldPasswordMismatch_throws() {
        // given
        AdminEntity admin = mock(AdminEntity.class);
        when(admin.getPassword()).thenReturn("ENCODED");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        PasswordChangeRequest req = mock(PasswordChangeRequest.class);
        when(req.getOldPassword()).thenReturn("old");

        when(passwordEncoder.matches("old", "ENCODED")).thenReturn(false);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> adminService.changePassword("admin", req));

        // then
        assertEquals(AdminSignupErrorCode.PASSWORD_MISMATCH, ex.getErrorCodeIfs());

        verify(adminRepository).findByUsername("admin");
        verify(passwordEncoder).matches("old", "ENCODED");
        verify(admin, never()).changePassword(anyString());
        verify(adminRepository, never()).save(any());
        verifyNoMoreInteractions(adminRepository, passwordEncoder);
    }




    @Test
    @DisplayName("changePassword: 정상이면 changePassword 호출 후 save")
    void changePassword_success_changesAndSaves() {
        AdminEntity admin = mock(AdminEntity.class);
        when(admin.getPassword()).thenReturn("ENCODED");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("old", "ENCODED")).thenReturn(true);
        when(passwordEncoder.encode("newStrong!Pass1")).thenReturn("NEW_ENCODED");

        PasswordChangeRequest req = mock(PasswordChangeRequest.class);
        when(req.getOldPassword()).thenReturn("old");
        when(req.getNewPassword()).thenReturn("newStrong!Pass1");

        adminService.changePassword("admin", req);

        verify(adminRepository).findByUsername("admin");
        verify(passwordEncoder).matches("old", "ENCODED");
        verify(passwordEncoder).encode("newStrong!Pass1");
        verify(admin).changePassword("NEW_ENCODED");
        verify(adminRepository).save(admin);
        verifyNoMoreInteractions(adminRepository, passwordEncoder);
    }
}