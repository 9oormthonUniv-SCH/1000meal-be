package com._1000meal.qr.roster;

import com._1000meal.holiday.service.HolidayScheduleGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RosterExportJob {

    private final RosterExportService rosterExportService;
    private final ObjectProvider<RosterSheetsSyncService> rosterSheetsSyncServiceProvider;
    private final HolidayScheduleGuard holidayScheduleGuard;

    // @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    // public void runDaily() {
    //     runOnce(LocalDate.now(KST));
    // }

    public void runOnce(LocalDate usedDate) {
        if (holidayScheduleGuard.shouldSkip("ROSTER_EXPORT", usedDate)) {
            return;
        }
        log.info("Roster export job started: usedDate={}", usedDate);
        Path mergedCsvPath = rosterExportService.exportDailyRosters(usedDate);

        rosterSheetsSyncServiceProvider.ifAvailable(sync -> {
            if (mergedCsvPath == null || !Files.exists(mergedCsvPath)) {
                log.warn("[CSV to Sheets] 통합 CSV 파일이 존재하지 않아 동기화를 건너뜁니다. usedDate={}, path={}",
                        usedDate, mergedCsvPath);
                return;
            }

            boolean ok = sync.syncRosterForDate(usedDate);
            if (ok) {
                log.info("[CSV to Sheets] export 후 동기화 완료. usedDate={}", usedDate);
            }
        });
    }
}
