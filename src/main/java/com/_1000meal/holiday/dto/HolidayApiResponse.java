package com._1000meal.holiday.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 공공데이터포털 휴일 정보 API(JSON) 응답 DTO.
 */
@Getter
@Setter
public class HolidayApiResponse {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Body body;
    }

    @Getter
    @Setter
    public static class Body {
        private Items items;
    }

    @Getter
    @Setter
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @Setter
    public static class Item {
        private String dateKind;
        private String dateName;
        private String isHoliday;
        private int locdate;
        private int seq;
    }
}

