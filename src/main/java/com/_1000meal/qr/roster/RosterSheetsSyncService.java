package com._1000meal.qr.roster;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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


//로컬에 생성된 QR 로스터 CSV를 Google Sheets에 동기화
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sheets.enabled", havingValue = "true")
public class RosterSheetsSyncService {

    private final Sheets sheets;

    @Value("${qr.roster.base-dir:./var/rosters}")
    private String baseDir;

    @Value("${sheets.spreadsheet-id:}")
    private String spreadsheetId;

    private static final int MAX_RETRY_ATTEMPTS = 4;
    private static final long INITIAL_DELAY_MS = 5000L;
    private static final long JITTER_MS = 500L;

    //해당 날짜 디렉터리의 통합 CSV(roster-*.csv)를 읽어 동명 시트에 덮어씀
    public boolean syncRosterForDate(LocalDate targetDate) {
        if (spreadsheetId == null || spreadsheetId.isBlank()) {
            log.warn("[CSV to Sheets] sheets.spreadsheet-id 가 설정되어 있지 않아 동기화를 건너뜁니다.");
            return false;
        }

        String sheetName = targetDate.toString();

        try {
            log.info("[CSV to Sheets] sync start. date={}, sheet={}", targetDate, sheetName);

            Path basePath = Path.of(baseDir).toAbsolutePath().normalize();
            Path dateDir = basePath.resolve(sheetName);
            if (!Files.exists(dateDir) || !Files.isDirectory(dateDir)) {
                log.warn("[CSV to Sheets] dateDir 가 존재하지 않습니다. dir={}", dateDir);
                return false;
            }

            List<Path> csvFiles;
            try (Stream<Path> stream = Files.list(dateDir)) {
                csvFiles = stream
                        .filter(p -> {
                            if (!Files.isRegularFile(p)) return false;
                            String name = p.getFileName().toString();
                            return name.startsWith("roster-") && name.endsWith(".csv");
                        })
                        .sorted()
                        .collect(Collectors.toList());
            }

            if (csvFiles.isEmpty()) {
                log.warn("[CSV to Sheets] roster-*.csv 가 없습니다. dir={}", dateDir);
                return false;
            }

            List<List<Object>> values = readCsvFiles(csvFiles);

            if (values.isEmpty()) {
                log.warn("[CSV to Sheets] CSV 내용이 비어 있습니다. dir={}", dateDir);
                return false;
            }

            ensureSheetExists(sheetName);
            retryOnSheetsError("clearSheet", () -> {
                clearSheet(sheetName);
                return null;
            });
            retryOnSheetsError("writeToSheet", () -> {
                writeToSheet(sheetName, values);
                return null;
            });

            log.info("[CSV to Sheets] 동기화 완료. date={}, rows={}", targetDate, values.size());
            return true;
        } catch (Exception e) {
            log.error("[CSV to Sheets] 동기화 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    //CSV 파일 읽어오기 및 헤더 확인
    private List<List<Object>> readCsvFiles(List<Path> csvFiles) throws IOException {
        List<List<Object>> values = new ArrayList<>();
        boolean headerSet = false;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .get();

        for (Path csv : csvFiles) {
            try (Reader reader = Files.newBufferedReader(csv, StandardCharsets.UTF_8);

            CSVParser parser = CSVParser.parse(reader, format)) {
                if (!headerSet) {
                    List<String> header = new ArrayList<>(parser.getHeaderNames());
                    // if (!header.isEmpty()) {
                    //     header.set(0, stripBom(header.get(0)));
                    // }
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

    //Byte Order Mark 제거
    //이를 제거하지 않으면 시트에 올릴 때 오류가 발생할 수도 있다고 합니다.
    //(필요시 사용 예정)
    // private String stripBom(String value) {
    //     if (value == null || value.isEmpty()) return value;
    //     if (value.charAt(0) == '\uFEFF') {
    //         return value.substring(1);
    //     }
    //     return value;
    // }

    //에러 발생 시 재시도
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

    //시트 존재 확인 및 생성
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

    //시트 데이터 초기화 (기존 데이터 삭제)
    private void clearSheet(String sheetName) throws IOException {
        ClearValuesRequest requestBody = new ClearValuesRequest();
        sheets.spreadsheets().values()
                .clear(spreadsheetId, sheetName + "!A:Z", requestBody)
                .execute();
    }

    //시트 쓰기
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
