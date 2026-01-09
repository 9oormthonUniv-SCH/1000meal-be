package com._1000meal.fcm.service;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.repository.AccountRepository; // 너 프로젝트 위치에 맞게 수정
import com._1000meal.fcm.domain.FcmSetting;
import com._1000meal.fcm.dto.FcmSettingResponse;
import com._1000meal.fcm.repository.FcmSettingRepository;
import com._1000meal.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmSettingService {

    private final AccountRepository accountRepository;
    private final FcmSettingRepository fcmSettingRepository;

    @Transactional(readOnly = true)
    public FcmSettingResponse getMySetting(Long accountId) {
        FcmSetting setting = fcmSettingRepository.findByAccount_Id(accountId)
                .orElse(null);

        if (setting == null) {
            // 설정이 없으면 “기본값(false, token 없음)”으로 응답
            return new FcmSettingResponse(false, false);
        }

        return new FcmSettingResponse(
                setting.isEnabled(),
                setting.getToken() != null && !setting.getToken().isBlank()
        );
    }

    @Transactional
    public void toggle(Long accountId, boolean enabled) {
        FcmSetting setting = fcmSettingRepository.findByAccount_Id(accountId)
                .orElseGet(() -> fcmSettingRepository.save(FcmSetting.create(getAccount(accountId))));

        if (enabled) setting.enable();
        else setting.disable();
        // dirty checking으로 save 호출 없어도 반영됨
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("ACCOUNT_NOT_FOUND"));
        // 너희 프로젝트 CustomException + ErrorCode로 바꿔주면 더 깔끔해
    }
}