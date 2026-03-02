package com._1000meal.qr.roster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class RosterExportJob {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RosterExportService rosterExportService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void runDaily() {
        LocalDate usedDate = LocalDate.now(KST);
        log.info("Roster export job started: usedDate={}", usedDate);
        rosterExportService.exportDailyRosters(usedDate);
    }

    public void runOnce(LocalDate usedDate) {
        rosterExportService.exportDailyRosters(usedDate);
    }
}
