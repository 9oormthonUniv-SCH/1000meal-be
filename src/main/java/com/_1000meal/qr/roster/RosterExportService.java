package com._1000meal.qr.roster;

import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.qr.domain.MealUsage;
import com._1000meal.qr.repository.MealUsageRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RosterExportService {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter USED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StoreRepository storeRepository;
    private final MealUsageRepository mealUsageRepository;
    private final MenuGroupRepository menuGroupRepository;

    @Value("${qr.roster.base-dir:./var/rosters}")
    private String baseDir;

    @Value("${qr.roster.file-encoding:UTF-8}")
    private String fileEncoding;

    @Value("${qr.roster.include-bom:true}")
    private boolean includeBom;

    public Path exportDailyRosters(LocalDate usedDate) {
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path dateDir = basePath.resolve(usedDate.toString());

        try {
            Files.createDirectories(dateDir);
        } catch (Exception e) {
            throw new IllegalStateException("로스터 디렉토리를 생성할 수 없습니다: " + dateDir, e);
        }

        List<MealUsage> usages = mealUsageRepository.findAllByUsedDateOrderByUsedAtAsc(usedDate);

        Map<Long, Store> storeById = storeRepository.findAll().stream()
                .collect(Collectors.toMap(Store::getId, store -> store));

        List<Long> groupIds = usages.stream()
                .map(MealUsage::getMenuGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> groupNameById = new HashMap<>();
        if (!groupIds.isEmpty()) {
            for (MenuGroup group : menuGroupRepository.findByIdIn(groupIds)) {
                groupNameById.put(group.getId(), group.getName());
            }
        }

        Map<GroupKey, List<MealUsage>> grouped = new HashMap<>();
        for (MealUsage usage : usages) {
            Long storeId = usage.getStore().getId();
            GroupKey key = new GroupKey(storeId, usage.getMenuGroupId());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(usage);
        }

        for (Map.Entry<GroupKey, List<MealUsage>> entry : grouped.entrySet()) {
            GroupKey key = entry.getKey();
            List<MealUsage> groupUsages = entry.getValue();
            Store store = storeById.get(key.storeId());
            String groupName = key.menuGroupId() == null ? "" : groupNameById.getOrDefault(key.menuGroupId(), "");

            Path outputPath = key.menuGroupId() == null
                    ? dateDir.resolve("store-" + key.storeId() + ".csv")
                    : dateDir.resolve("store-" + key.storeId() + "-group-" + key.menuGroupId() + ".csv");

            writeCsv(outputPath, store, groupName, groupUsages);
            log.info("Roster export done: usedDate={}, storeId={}, menuGroupId={}, rowCount={}, outputPath={} ",
                    usedDate, key.storeId(), key.menuGroupId(), groupUsages.size(), outputPath);
        }

        return exportMergedDailyRoster(usedDate, dateDir, storeById, groupNameById);
    }

    private void writeCsv(Path outputPath, Store store, String groupName, List<MealUsage> usages) {
        Charset charset = Charset.forName(fileEncoding);
        String storeName = store == null || store.getName() == null ? "" : store.getName();

        Path tempPath = createTempFile(outputPath);
        try (OutputStream os = Files.newOutputStream(
                tempPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
             OutputStreamWriter osw = new OutputStreamWriter(os, charset);
             BufferedWriter writer = new BufferedWriter(osw)
        ) {
            if (includeBom && "UTF-8".equalsIgnoreCase(fileEncoding)) {
                os.write(UTF8_BOM);
            }

            //writer.write("학과,학번,이름,매장명,그룹명,인식시간,수량");
            writer.write("학과,학번,이름,매장명,인식시간,수량");
            writer.newLine();

            for (MealUsage usage : usages) {
                String dept = safe(usage.getDeptSnapshot());
                String studentNo = safe(usage.getStudentNoSnapshot());
                String name = safe(usage.getNameSnapshot());
                String usedAt = usage.getUsedAt() == null ? "" : usage.getUsedAt().format(USED_AT_FORMAT);

                writer.write(csvLine(dept, studentNo, name, storeName, groupName, usedAt, "1"));
                writer.newLine();
            }
            moveAtomically(tempPath, outputPath);
        } catch (Exception e) {
            throw new IllegalStateException("로스터 CSV 생성에 실패했습니다: " + outputPath, e);
        }
    }

    private String csvLine(String... values) {
        return java.util.Arrays.stream(values)
                .map(this::escapeCsv)
                .collect(Collectors.joining(","));
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuote = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Path exportMergedDailyRoster(
            LocalDate usedDate,
            Path dateDir,
            Map<Long, Store> storeById,
            Map<Long, String> groupNameById
    ) {
        List<MealUsage> merged = mealUsageRepository.findAllByUsedDateOrderByStoreAndGroupAndUsedAt(usedDate);

        Path outputPath = dateDir.resolve("roster-" + usedDate + ".csv");
        Charset charset = Charset.forName(fileEncoding);
        Path tempPath = createTempFile(outputPath);

        Set<Long> missingGroupNames = new HashSet<>();

        try (OutputStream os = Files.newOutputStream(
                tempPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
             OutputStreamWriter osw = new OutputStreamWriter(os, charset);
             BufferedWriter writer = new BufferedWriter(osw)
        ) {
            if (includeBom && "UTF-8".equalsIgnoreCase(fileEncoding)) {
                os.write(UTF8_BOM);
            }


            writer.write("학과,학번,이름,매장명,인식시간,수량");
            writer.newLine();

            for (MealUsage usage : merged) {
                String dept = safe(usage.getDeptSnapshot());
                String studentNo = safe(usage.getStudentNoSnapshot());
                String name = safe(usage.getNameSnapshot());
                // Long storeId = usage.getStore().getId();
                // Store store = storeById.get(storeId);
                // String storeName = store == null || store.getName() == null ? "" : store.getName();
                Long menuGroupId = usage.getMenuGroupId();
                String groupName = "";
                if (menuGroupId != null) {
                    groupName = groupNameById.get(menuGroupId);
                    if (groupName == null) {
                        if (missingGroupNames.add(menuGroupId)) {
                            log.warn("Roster export missing group name: menuGroupId={}", menuGroupId);
                        }
                        groupName = "(알 수 없음)";
                    }
                }

                String usedAt = usage.getUsedAt() == null ? "" : usage.getUsedAt().format(USED_AT_FORMAT);

                //writer.write(csvLine(dept, studentNo, name, storeName, groupName, usedAt, "1"));
                writer.write(csvLine(dept, studentNo, name, groupName, usedAt, "1"));
                writer.newLine();
            }

            moveAtomically(tempPath, outputPath);
            log.info("Merged roster export done: usedDate={}, rowCount={}, outputPath={} ",
                    usedDate, merged.size(), outputPath);
            return outputPath;
        } catch (Exception e) {
            throw new IllegalStateException("통합 로스터 CSV 생성에 실패했습니다: " + outputPath, e);
        }
    }

    private Path createTempFile(Path outputPath) {
        try {
            Path dir = outputPath.getParent();
            return Files.createTempFile(dir, outputPath.getFileName().toString(), ".tmp");
        } catch (Exception e) {
            throw new IllegalStateException("임시 파일 생성에 실패했습니다: " + outputPath, e);
        }
    }

    private void moveAtomically(Path tempPath, Path outputPath) throws Exception {
        try {
            Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record GroupKey(Long storeId, Long menuGroupId) {
    }
}
