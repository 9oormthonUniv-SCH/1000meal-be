package com._1000meal.qr.roster;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

// Local에서만 쓰고 나중에 지워도 됨
@Profile("local")
@Component
@RequiredArgsConstructor
public class RosterExportLocalRunner implements CommandLineRunner {
    private final RosterExportJob rosterExportJob;

    @Override
    public void run(String... args) {
        rosterExportJob.runOnce(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }
}