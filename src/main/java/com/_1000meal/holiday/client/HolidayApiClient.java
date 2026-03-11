package com._1000meal.holiday.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 공공데이터포털 휴일 정보 API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${api.holiday.service-key}")
    private String serviceKey;

    public String getRestHolidaysJson(int year) {

        URI uri = UriComponentsBuilder
                .fromHttpUrl("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
                .queryParam("serviceKey", serviceKey)
                .queryParam("solYear", year)
                .queryParam("_type", "json")
                .queryParam("numOfRows", 200)
                .build(true)
                .toUri();

        log.info("[HOLIDAY-API] request uri={}", uri);
        return restTemplate.getForObject(uri, String.class);
    }
}

