package com._1000meal.fcm.scheduler;

import com._1000meal.fcm.domain.FcmToken;
import com._1000meal.fcm.repository.FcmTokenRepository;
import com._1000meal.fcm.repository.NotificationPreferenceRepository;
import com._1000meal.fcm.sender.FcmSendResult;
import com._1000meal.fcm.sender.FcmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 평일 07:50에 오픈 10분 전 공통 알림을 보내는 스케줄러.
 * - 알림 동의한 모든 계정 대상
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenReminderScheduler {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmSender fcmSender;

    // 평일(월~금) 07:50 KST
    @Scheduled(cron = "0 50 7 * * MON-FRI", zone = "Asia/Seoul")
    public void sendOpenReminder() {
        LocalDate today = LocalDate.now(ZONE_ID);
        log.info("[FCM][OPEN_REMINDER] scheduler tick. date={}", today);

        List<Long> enabledAccountIds = notificationPreferenceRepository.findEnabledAccountIds();
        if (enabledAccountIds.isEmpty()) {
            log.info("[FCM][OPEN_REMINDER] enabledAccountIds empty. skip");
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findAllByAccountIdInAndActiveTrue(enabledAccountIds);
        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getToken)
                .distinct()
                .collect(Collectors.toList());

        if (tokenStrings.isEmpty()) {
            log.info("[FCM][OPEN_REMINDER] active tokens empty. skip");
            return;
        }

        String title = "천원의 아침밥 오픈 사전 알림";
        String body  = "10분 후 천원의 아침밥이 시작돼요.";

        Map<String, String> data = Map.of(
                "type", "OPEN_REMINDER"
        );

        try {
            FcmSendResult result = fcmSender.sendMulticast(tokenStrings, title, body, data);
            log.info("[FCM][OPEN_REMINDER] success={}, failure={}",
                    result.successCount(), result.failureCount());
        } catch (Exception e) {
            log.error("[FCM][OPEN_REMINDER] send failed: {}", e.getMessage(), e);
        }
    }
}
