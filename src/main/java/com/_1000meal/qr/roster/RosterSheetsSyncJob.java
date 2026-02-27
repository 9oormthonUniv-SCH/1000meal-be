package com._1000meal.qr.roster;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 로컬에 생성된 QR 로스터 CSV를 Google Sheets에 동기화
 *
 * 데이터 소스: qr.roster.base-dir (기본 ./var/rosters) 아래의 {yyyy-MM-dd} 디렉토리.
 * - 각 디렉토리 내의 모든 *.csv 파일을 읽어, 헤더 1줄 + 데이터 행들로 합쳐서
 *   "yyyy-MM-dd" 이름의 시트에 A1부터 덮어쓴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sheets.enabled", havingValue = "true")
public class RosterSheetsSyncJob {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final Sheets sheets;

    @Value("${qr.roster.base-dir:./var/rosters}")
    private String baseDir;

    @Value("${sheets.spreadsheet-id:}")
    private String spreadsheetId;

    private static final int MAX_RETRY_ATTEMPTS = 4;    // 최대 재시도 횟수
    private static final long INITIAL_DELAY_MS = 5000L; // 초기 딜레이 시간
    private static final long JITTER_MS = 500L; // 추가 딜레이 증가 값

    // 운영환경에서는 (00 00 11 ? * MON-FRI) 로 변경 예정
    @Scheduled(cron = "00 00 * * * *", zone = "Asia/Seoul") // 테스트 진행을 위해 00 00 * * * * 로 설정
    public void syncDailyRoster() {
        if (spreadsheetId == null || spreadsheetId.isBlank()) {
            log.warn("[CSV to Sheets] sheets.spreadsheet-id 가 설정되어 있지 않아 동기화를 건너뜁니다.");
            return;
        }

        // 오늘 날짜 기준으로 시트 이름 결정
        LocalDate targetDate = LocalDate.now(KST);
        String sheetName = targetDate.toString();

        try {
            log.info("[CSV to Sheets] sync start. date={}, sheet={}", targetDate, sheetName);

            Path basePath = Path.of(baseDir).toAbsolutePath().normalize();
            Path dateDir = basePath.resolve(sheetName);
            if (!Files.exists(dateDir) || !Files.isDirectory(dateDir)) {
                log.info("[CSV to Sheets] dateDir 가 존재하지 않습니다. 스킵 진행. dir={}", dateDir);
                return;
            }

            // 해당 날짜 디렉토리 내의 모든 CSV 파일 목록 조회 진행
            List<Path> csvFiles;
            try (Stream<Path> stream = Files.list(dateDir)) {
                csvFiles = stream
                        .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".csv"))
                        .sorted()
                        .collect(Collectors.toList());
            }

            if (csvFiles.isEmpty()) {
                log.info("[CSV to Sheets] CSV 파일이 존재하지 않습니다. 스킵 진행. dir={}", dateDir);
                return;
            }

            // CSV 파일 읽어오기 및 헤더 확인 
            List<List<Object>> values = readCsvFiles(csvFiles);
            if (values.size() <= 1) {
                log.info("[CSV to Sheets] 헤더만 존재하거나 비어있습니다. 스킵 진행. dir={}", dateDir);
                return;
            }

            // 시트 존재 확인 및 생성 진행
            ensureSheetExists(sheetName);
            
            // 시트 데이터 초기화 진행 (기존 데이터 삭제)
            retryOnSheetsError("clearSheet", () -> {
                clearSheet(sheetName);
                return null;
            });
            // 시트 쓰기 진행
            retryOnSheetsError("writeToSheet", () -> {
                writeToSheet(sheetName, values);
                return null;
            });

            log.info("[CSV to Sheets] 동기화 완료. date={}, rows={}", targetDate, values.size());
        } catch (Exception e) {
            log.error("[CSV to Sheets] 동기화 실패: {}", e.getMessage(), e);
        }
    }
    
    // CSV 파일 읽어오기 및 헤더 확인 
    private List<List<Object>> readCsvFiles(List<Path> csvFiles) throws IOException {
        List<List<Object>> values = new ArrayList<>();
        boolean headerSet = false;

        // CSV 파일 포맷
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .build();

        // CSV 파일 읽어오기 및 헤더 확인 
        for (Path csv : csvFiles) {
            try (Reader reader = Files.newBufferedReader(csv, StandardCharsets.UTF_8);
                 CSVParser parser = new CSVParser(reader, format)) {

                if (!headerSet) {
                    List<String> header = new ArrayList<>(parser.getHeaderNames());
                    if (!header.isEmpty()) {
                        header.set(0, stripBom(header.get(0)));
                    }
                    values.add(new ArrayList<>(header));
                    headerSet = true;
                }

                for (CSVRecord record : parser) {
                    List<Object> row = new ArrayList<>(record.size());
                    record.forEach(row::add);
                    values.add(row);
                }
            }
        }

        return values;
    }

    // Byte Order Mark 제거
    // 이를 제거하지 않으면 시트에 올릴 때 오류가 발생할 수도 있다고 합니다.
    private String stripBom(String value) {
        if (value == null || value.isEmpty()) return value;
        if (value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }

    // 차후 필요 시 사용 예정 : A1 표기 시 시트명에 특수문자(하이픈 등)가 있으면 작은따옴표로 감싼다. (에러 방지용도 입니다.)
    // private String toA1Range(String sheetName, String range) {
    //     return "'" + sheetName.replace("'", "''") + "'!" + range;
    // }

    /** 에러 발생 시 재시도 */
    private <T> T retryOnSheetsError(String opName, Callable<T> action) throws Exception {
        long delayMs = INITIAL_DELAY_MS;
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return action.call();
            } catch (GoogleJsonResponseException e) {
                int code = e.getStatusCode();
                String reason = null;
                if (e.getDetails() != null && e.getDetails().getErrors() != null && !e.getDetails().getErrors().isEmpty()) {
                    reason = e.getDetails().getErrors().get(0).getReason();
                }
                boolean retryable = code == 500 || code == 502 || code == 503 || code == 504
                        || "backendError".equals(reason);
                if (!retryable || attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                long sleep = delayMs + ThreadLocalRandom.current().nextLong(0, JITTER_MS);
                log.warn("[CSV to Sheets] {} 실패 (시도 {}/{}), {}ms 후 재시도. code={}, reason={}",
                        opName, attempt, MAX_RETRY_ATTEMPTS, sleep, code, reason);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
                delayMs *= 2;
            } catch (IOException e) {
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException(e);
                }
                long sleep = delayMs + ThreadLocalRandom.current().nextLong(0, JITTER_MS);
                log.warn("[CSV to Sheets] {} IO 실패 (시도 {}/{}), {}ms 후 재시도.",
                        opName, attempt, MAX_RETRY_ATTEMPTS, sleep);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
                delayMs *= 2;
            }
        }
        throw new IllegalStateException("unreachable");
    }

    // 시트 존재 확인 및 생성
    private void ensureSheetExists(String sheetName) throws IOException {
        Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();
        boolean exists = spreadsheet.getSheets() != null &&
                spreadsheet.getSheets().stream()
                        .map(Sheet::getProperties)
                        .map(SheetProperties::getTitle)
                        .anyMatch(sheetName::equals);

        if (exists) {
            return;
        }

        log.info("[CSV to Sheets] 시트가 존재하지 않습니다. 생성 진행. sheet={}", sheetName);

        SheetProperties properties = new SheetProperties().setTitle(sheetName);
        Request addSheetRequest = new Request().setAddSheet(
                new com.google.api.services.sheets.v4.model.AddSheetRequest().setProperties(properties)
        );

        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(addSheetRequest));

        sheets.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute();
    }

    // 시트 데이터 초기화 (기존 데이터 삭제)
    private void clearSheet(String sheetName) throws IOException {
        ClearValuesRequest requestBody = new ClearValuesRequest();
        sheets.spreadsheets().values()
                .clear(spreadsheetId, sheetName + "!A:Z", requestBody)
                .execute();
    }

    // 시트 쓰기
    private void writeToSheet(String sheetName, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse response = sheets.spreadsheets().values()
                .update(spreadsheetId, sheetName + "!A1", body)
                .setValueInputOption("RAW")
                .execute();

        log.info("[CSV to Sheets] update result: updatedRows={}, updatedColumns={}",
                response.getUpdatedRows(), response.getUpdatedColumns());
    }
}

