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
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.security.JwtProvider;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;   // ✅ 추가
import java.util.Map;      // ✅ 추가

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepo;
    private final UserProfileRepository userProfileRepo;
    private final AdminProfileRepository adminProfileRepo;
    private final StoreRepository storeRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;

    /* -------------------- 회원가입 -------------------- */
    @Transactional
    public SignupResponse signup(SignupRequest req) {
        if (req.role() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "역할(role)은 필수입니다.");
        }

        final String rawUserId = req.userId();
        final String rawEmail  = req.email();
        final String rawPwd    = req.password();

        // 입력 정규화
        final String userId = (rawUserId == null) ? "" : rawUserId.trim();
        final String email  = (rawEmail  == null) ? "" : rawEmail.trim().toLowerCase();
        final Role   role   = req.role();

        // --- 기본 형식 검증 ---
        if (userId.isEmpty() || email.isEmpty() || rawPwd == null || rawPwd.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "아이디/이메일/비밀번호는 필수입니다.");
        }

        // --- 비밀번호 정책 검증: 인코딩 전에 수행 ---
        // username 파라미터는 정책상 금지 문자열 체크를 위해 userId 전달 (전화번호는 없으면 null)
        com._1000meal.global.util.PasswordValidator.validatePassword(rawPwd, userId, null);

        // --- 유니크 검사 (DELETED 제외) ---
        if (accountRepo.existsByUserIdAndStatusNot(userId, AccountStatus.DELETED)) {
            throw new CustomException(ErrorCode.DUPLICATE_USER_ID);
        }
        if (accountRepo.existsByEmailAndStatusNot(email, AccountStatus.DELETED)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // --- 역할별 정책 ---
        AccountStatus status = AccountStatus.ACTIVE;
        if (role == Role.STUDENT) {
            // (1) 학번 형식: 8자리 숫자
            if (!userId.matches("^\\d{8}$")) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "학번 형식이 올바르지 않습니다.");
            }
            // (2) 이메일 도메인
            if (!email.endsWith("@sch.ac.kr")) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "학생 계정은 @sch.ac.kr 이메일만 허용됩니다.");
            }
            // (3) 이메일 인증 선행 확인
            emailService.requireVerified(email);
            status = AccountStatus.ACTIVE;
        }

        // --- Account 저장 ---
        Account account = new Account(
                null,
                userId,
                email,
                passwordEncoder.encode(rawPwd),  // ← 검증 통과 후 인코딩
                role,
                status
        );
        accountRepo.save(account);

        // --- 프로필 & 스토어(관리자만) ---
        Long storeId = null;
        String storeName = null;

        if (role == Role.STUDENT) {
            UserProfile profile = UserProfile.create(account, null, req.name(), null);
            userProfileRepo.save(profile);
            // 가입 성공 후 해당 이메일의 인증코드 일괄 소진
            emailService.consumeAllFor(email);
        } else { // ADMIN
            if (req.storeId() == null) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "관리자 가입 시 storeId는 필수입니다.");
            }
            Store store = storeRepo.findById(req.storeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 storeId의 매장을 찾을 수 없습니다."));
            AdminProfile profile = AdminProfile.create(account, req.name(), 1, store);
            adminProfileRepo.save(profile);

            storeId = store.getId();
            storeName = store.getName();
        }

        // --- 응답 ---
        return new SignupResponse(
                account.getId(),
                account.getRole(),
                account.getUserId(),
                account.getEmail(),
                account.getStatus().name(),
                storeId,
                storeName
        );
    }

    /* -------------------- 로그인 -------------------- */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        final String userId = req.userId().trim();
        final Role reqRole  = req.role(); // 요청된 역할

        // 1) 계정 조회 (userId 기준)
        Account account = accountRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 2) 비밀번호 체크
        if (!passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3) 역할 일치 체크 (핵심)
        if (account.getRole() != reqRole) {
            // 프론트가 표시하기 쉬운 메시지로
            throw new IllegalArgumentException("역할이 일치하지 않습니다. (" + reqRole + "로 로그인할 수 없습니다)");
        }

        // 4) 상태 체크
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("계정이 활성화되지 않았습니다.");
        }

        // 5) 표시 이름/가게 정보 조회 (생략 가능: 기존 코드 재사용)
        String displayName = resolveName(account);
        Long storeId = null;
        String storeName = null;
        if (account.getRole() == Role.ADMIN) {
            var ap = adminProfileRepo.findByAccountId(account.getId()).orElse(null);
            if (ap != null && ap.getStore() != null) {
                storeId = ap.getStore().getId();
                storeName = ap.getStore().getName();
            }
        }

        // 6) 토큰 발급 (role은 DB 값 사용)
        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUserId(),
                displayName,
                account.getEmail(),
                account.getRole().name()
        );
        Map<String,Object> extra = (storeId == null) ? null : Map.of("storeId", storeId, "storeName", storeName);
        String accessToken = jwtProvider.createToken(principal, extra);

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUserId(),
                account.getEmail(),
                accessToken,
                null,
                storeId,
                storeName
        );
    }

    /* -------------------- 내 정보 -------------------- */
    @Transactional(readOnly = true)
    public LoginResponse me(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        Account account = accountRepo.findById(principal.id())
                .orElseThrow(() -> new IllegalStateException("계정이 존재하지 않습니다."));

        // ADMIN이면 상점 정보도 함께 내려줌(일관성)
        Long storeId = null;
        String storeName = null;
        if (account.getRole() == Role.ADMIN) {
            AdminProfile ap = adminProfileRepo.findByAccountId(account.getId()).orElse(null);
            if (ap != null && ap.getStore() != null) {
                storeId = ap.getStore().getId();
                storeName = ap.getStore().getName();
            }
        }

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUserId(),
                account.getEmail(),
                null,   // accessToken 없음
                null,   // refreshToken 없음
                storeId,
                storeName
        );
    }

    /* 표시 이름 조회(역할별 프로필에서) */
    private String resolveName(Account account) {
        if (account.getRole() == Role.STUDENT) {
            return userProfileRepo.findByAccountId(account.getId())
                    .map(UserProfile::getName)
                    .orElse(null);
        } else { // ADMIN
            return adminProfileRepo.findByAccountId(account.getId())
                    .map(AdminProfile::getDisplayName)
                    .orElse(null);
        }
    }
}