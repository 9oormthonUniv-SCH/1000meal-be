package com._1000meal.qr.service;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.UserProfile;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.qr.exception.MissingStudentNumberException;
import com._1000meal.qr.api.dto.QrUsageResponse;
import com._1000meal.qr.domain.MealUsage;
import com._1000meal.qr.domain.StoreQr;
import com._1000meal.qr.repository.MealUsageRepository;
import com._1000meal.qr.repository.StoreQrRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QrUsageService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StoreQrRepository storeQrRepository;
    private final MealUsageRepository mealUsageRepository;
    private final StoreRepository storeRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public QrUsageResponse createUsage(Long accountId, String qrToken) {
        StoreQr storeQr = storeQrRepository.findByQrTokenAndIsActiveTrue(qrToken)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "QR을 찾을 수 없습니다."));

        Store store = storeRepository.findById(storeQr.getStore().getId())
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 프로필을 찾을 수 없습니다."));

        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        LocalDate usedDate = nowKst.toLocalDate();
        LocalDateTime usedAt = nowKst.toLocalDateTime();

        String studentNoSnapshot = account.getUserId();
        if (studentNoSnapshot == null || studentNoSnapshot.isBlank()) {
            throw new MissingStudentNumberException();
        }

        String deptSnapshot = profile.getDepartment() == null ? "" : profile.getDepartment();
        String nameSnapshot = profile.getName() == null ? "" : profile.getName();

        MealUsage mealUsage = MealUsage.create(
                account,
                store,
                usedAt,
                usedDate,
                deptSnapshot,
                studentNoSnapshot,
                nameSnapshot
        );

        try {
            mealUsageRepository.save(mealUsage);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CONFLICT, "오늘 이미 이용했습니다.");
        }

        return new QrUsageResponse(
                store.getId(),
                store.getName(),
                nowKst.toOffsetDateTime().toString(),
                usedDate
        );
    }
}
