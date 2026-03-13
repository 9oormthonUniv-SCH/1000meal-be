package com._1000meal.fcm.scheduler;

import com._1000meal.menu.event.WeeklyMenuUploadedEvent;
import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyMenuNotificationScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StoreRepository storeRepository;
    private final MenuGroupService menuGroupService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 매주 일요일 18:00(KST)에 다음 주 월~금 메뉴 입력 상태를 점검합니다.
     *
     * 이미 5일치 메뉴가 모두 입력된 그룹이 있으면 기존 이벤트를 다시 발행해서,
     * 현재 사용 중인 리스너가 구독자 조회, 중복 방지, FCM 발송을 그대로 처리하게 합니다.
     * 즉 이 스케줄러는 "언제 보낼지"만 담당하고, "어떻게 보낼지"는 기존 흐름을 재사용합니다.
     */
    @Scheduled(cron = "0 0 18 * * SUN", zone = "Asia/Seoul")
//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void notifyCompletedWeeklyMenus() {
        LocalDate today = LocalDate.now(KST);
        // 일요일 18시에 보내는 알림은 "다음 날 시작하는 주"를 기준으로 검사합니다.
        LocalDate targetWeekStart = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        String weekKey = targetWeekStart.format(DateTimeFormatter.ISO_DATE);

        log.info("[스케줄러][WEEKLY_MENU_UPLOADED] scheduler tick. today={}, targetWeekStart={}",
                today, targetWeekStart);

        List<Long> storeIds = storeRepository.findAllStoreIds();
        if (storeIds.isEmpty()) {
            log.info("[스케줄러][WEEKLY_MENU_UPLOADED] no stores found. skip");
            return;
        }

        for (Long storeId : storeIds) {
            // 해당 매장에서 월~금 5일치 메뉴가 모두 입력된 group만 추립니다.
            List<Long> completedGroupIds = menuGroupService.findCompletedWeeklyMenuGroupIds(storeId, targetWeekStart);
            if (completedGroupIds.isEmpty()) {
                continue;
            }

            // 이벤트만 발행하고 실제 알림 처리(구독자 조회, dedupe, FCM)는 기존 리스너에 맡깁니다.
            eventPublisher.publishEvent(
                    new WeeklyMenuUploadedEvent(storeId, completedGroupIds, weekKey, targetWeekStart)
            );

            log.info("[스케줄러][WEEKLY_MENU_UPLOADED] published storeId={}, groups={}, weekKey={}",
                    storeId, completedGroupIds.size(), weekKey);
        }
    }
}
