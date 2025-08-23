package com._1000meal.auth.service;

import com._1000meal.auth.dto.*;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AdminProfile;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.model.UserProfile;
import com._1000meal.auth.repository.*;
import com._1000meal.email.service.EmailService;
import com._1000meal.global.constant.Role;
import com._1000meal.global.security.JwtProvider;
import com._1000meal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepo;
    private final UserProfileRepository userProfileRepo;
    private final AdminProfileRepository adminProfileRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        // 0) 입력 정규화(선택)
        final String username = req.username().trim();
        final String email = req.email().trim().toLowerCase();

        // 1) 유니크 검사
        if (accountRepo.existsByUsernameOrEmail(username, email)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디/이메일입니다.");
        }

        if (req.role() == Role.STUDENT && !email.endsWith("@sch.ac.kr")) {
            throw new IllegalArgumentException("학생 계정은 @sch.ac.kr 이메일만 허용됩니다.");
        }

        // 2) 상태 결정: ADMIN은 즉시 ACTIVE, STUDENT는 이메일 인증 시 ACTIVE
        final String status =
                (req.role() == Role.ADMIN) ? "ACTIVE"
                        : (emailService.isEmailVerified(email) ? "ACTIVE" : "PENDING");

        // 3) Account 생성/저장 (결정된 status 사용)
        var account = new Account(
                null,
                username,
                email,
                passwordEncoder.encode(req.password()),
                req.role(),     // STUDENT / ADMIN
                status          // ★ 여기!
        );
        accountRepo.save(account);

        // 4) 역할별 프로필 저장
        if (req.role() == Role.STUDENT) {
            userProfileRepo.save(new UserProfile(account.getId(), req.department(), req.name(), req.phone()));
        } else {
            adminProfileRepo.save(new AdminProfile(account.getId(), req.displayName(), defaultLevel(req.adminLevel())));
        }

        // 5) 응답
        return new SignupResponse(
                account.getId(),
                account.getRole(),
                account.getUsername(),
                account.getEmail(),
                account.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        var account = accountRepo.findByRoleAndIdentifier(req.role(), req.usernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUsername(),
                resolveName(account),
                account.getEmail(),
                account.getRole().name()
        );
        String accessToken = jwtProvider.createToken(principal);

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUsername(),
                account.getEmail(),
                accessToken,
                null
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse me(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        var account = accountRepo.findById(principal.id())
                .orElseThrow(() -> new IllegalStateException("계정이 존재하지 않습니다."));

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUsername(),
                account.getEmail(),
                null,
                null
        );
    }

    private String resolveName(Account account) {
        if (account.getRole() == Role.STUDENT) {
            return userProfileRepo.findById(account.getId()).map(UserProfile::getName).orElse(null);
        } else {
            return adminProfileRepo.findById(account.getId()).map(AdminProfile::getDisplayName).orElse(null);
        }
    }

    private int defaultLevel(Integer lvl) { return (lvl == null ? 1 : lvl); }
}