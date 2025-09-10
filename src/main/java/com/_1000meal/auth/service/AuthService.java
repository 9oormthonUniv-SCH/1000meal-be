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
        // 0) 필수 필드/기본 정규화
        if (req.role() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "역할(role)은 필수입니다.");
        }
        final String rawUserId = req.userId();
        final String rawEmail  = req.email();
        final String rawPwd    = req.password();
        final String name      = req.name(); // null 허용하되 trim은 해 둠

        final String userId = (rawUserId == null) ? "" : rawUserId.trim();
        final String email  = (rawEmail  == null) ? "" : rawEmail.trim().toLowerCase();
        final String nameNm = (name == null) ? null : name.trim();
        final Role   role   = req.role();

        // 1) 입력 형식 검증
        if (userId.isEmpty() || email.isEmpty() || rawPwd == null || rawPwd.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "아이디/이메일/비밀번호는 필수입니다.");
        }

        // 2) 비밀번호 정책 (인코딩 전)
        com._1000meal.global.util.PasswordValidator.validatePassword(rawPwd, userId, null);

        // 3) 역할별 정책
        AccountStatus status = AccountStatus.ACTIVE;
        if (role == Role.STUDENT) {
            if (!userId.matches("^\\d{8}$")) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "학번 형식이 올바르지 않습니다. (예: 8자리 숫자)");
            }
            if (!email.endsWith("@sch.ac.kr")) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "학생 계정은 @sch.ac.kr 이메일만 허용됩니다.");
            }
            // 이메일 인증 선행
            emailService.requireVerified(email);
        } else if (role == Role.ADMIN) {
            if (req.storeId() == null) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "관리자 가입 시 storeId는 필수입니다.");
            }
        }

        // 4) 중복 검사 (DELETED 제외)
        if (accountRepo.existsByUserIdAndStatusNot(userId, AccountStatus.DELETED)) {
            throw new CustomException(ErrorCode.DUPLICATE_USER_ID, "이미 사용 중인 아이디입니다.");
        }
        if (accountRepo.existsByEmailAndStatusNot(email, AccountStatus.DELETED)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        // 5) 저장 (경합 대비: DB Unique 에러를 캐치해 사용자 친화 메시지로 변환)
        Account account;
        try {
            account = new Account(
                    null,
                    userId,
                    email,
                    passwordEncoder.encode(rawPwd),
                    role,
                    status
            );
            accountRepo.save(account);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // DB Unique 제약(이메일/아이디) 충돌 시 안전망
            String msg = String.valueOf(e.getMostSpecificCause()).toLowerCase();
            if (msg.contains("email") || msg.contains("uk_") && msg.contains("email")) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
            }
            if (msg.contains("user_id") || msg.contains("uk_") && msg.contains("user_id")) {
                throw new CustomException(ErrorCode.DUPLICATE_USER_ID, "이미 사용 중인 아이디입니다.");
            }
            throw new CustomException(ErrorCode.CONFLICT, "요청이 현재 리소스 상태와 충돌합니다."); // 기타 케이스
        }

        // 6) 프로필/스토어
        Long storeId = null;
        String storeName = null;
        if (role == Role.STUDENT) {
            UserProfile profile = UserProfile.create(account, null, nameNm, null);
            userProfileRepo.save(profile);
            // 가입 성공 후 해당 이메일의 인증코드 소진
            emailService.consumeAllFor(email);
        } else { // ADMIN
            Store store = storeRepo.findById(req.storeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 storeId의 매장을 찾을 수 없습니다."));
            AdminProfile profile = AdminProfile.create(account, nameNm, 1, store);
            adminProfileRepo.save(profile);
            storeId = store.getId();
            storeName = store.getName();
        }

        // 7) 응답
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
        final String rawUserId = req.userId();
        final Role reqRole     = req.role();

        if (rawUserId == null || rawUserId.isBlank() || req.password() == null || reqRole == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "아이디/비밀번호/역할은 필수입니다.");
        }
        final String userId = rawUserId.trim();

        // (권장) 삭제 계정 제외하고 조회하도록 레포지토리 메서드 추가해 두면 더 안전
        // Optional<Account> findByUserIdAndStatus(String userId, AccountStatus status);
        Account account = accountRepo.findByUserId(userId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 확인 (실패 시 동일 메시지로 모模호화)
        if (!passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 역할 일치 체크
        if (account.getRole() != reqRole) {
            throw new CustomException(ErrorCode.ROLE_MISMATCH,
                    "역할이 일치하지 않습니다. (" + reqRole + "로 로그인할 수 없습니다)");
        }

        // 상태 체크
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_INACTIVE, "계정이 활성화되지 않았습니다.");
        }

        // 표시 이름/가게 정보
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

        // 토큰 발급
        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUserId(),
                displayName,
                account.getEmail(),
                account.getRole().name()
        );
        Map<String, Object> extra = (storeId == null) ? null : Map.of("storeId", storeId, "storeName", storeName);
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
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "계정이 존재하지 않습니다."));

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