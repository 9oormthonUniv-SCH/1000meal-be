package com._1000meal.qr.retention;

import com._1000meal.qr.repository.MealUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionCleanupService {

    private final MealUsageRepository mealUsageRepository;

    @Value("${qr.roster.base-dir:./var/rosters}")
    private String baseDir;

    @Transactional
    public void cleanup(LocalDate cutoffDate) {
        int deletedRows = mealUsageRepository.deleteByUsedDateBefore(cutoffDate);
        int deletedDirs = deleteOldDirectories(cutoffDate);

        log.info("Retention cleanup done: cutoffDate={}, deletedRows={}, deletedDirs={}",
                cutoffDate, deletedRows, deletedDirs);
    }

    private int deleteOldDirectories(LocalDate cutoffDate) {
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        if (basePath.toString().equals("/")) {
            log.warn("Retention cleanup skipped: baseDir is root '/' ");
            return 0;
        }
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            log.info("Retention cleanup skipped: baseDir not found: {}", basePath);
            return 0;
        }

        AtomicInteger deletedCount = new AtomicInteger();
        try (var stream = Files.list(basePath)) {
            stream.filter(Files::isDirectory).forEach(dir -> {
                LocalDate dirDate = parseDateDir(dir.getFileName().toString());
                if (dirDate != null && dirDate.isBefore(cutoffDate)) {
                    if (deleteRecursively(dir)) {
                        deletedCount.incrementAndGet();
                        log.info("Retention cleanup removed dir: {}", dir);
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("로스터 폴더 정리에 실패했습니다: " + basePath, e);
        }

        return deletedCount.get();
    }

    private LocalDate parseDateDir(String name) {
        try {
            return LocalDate.parse(name);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean deleteRecursively(Path path) {
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception e) {
                    throw new IllegalStateException("삭제 실패: " + p, e);
                }
            });
            return true;
        } catch (Exception e) {
            log.warn("디렉토리 삭제 실패: {}", path, e);
            return false;
        }
    }
}
