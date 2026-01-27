package com._1000meal.store.domain;

import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    private String name;          // 매장 이름
    private String address;       // 위치주소
    private String phone;         // 전화번호
    private String description;   // 간단한 설명

    private LocalTime openTime;   // 영업시작시간
    private LocalTime closeTime;  // 영업종료시간
    private boolean isOpen;

    private int remain;           // 남은 수량
    private String hours;         // 운영 시간 문자열 ("08:00 ~ 소진 시")

    private double lat;           // 위도
    private double lng;           // 경도

    // 이전 주간 메뉴 이력은 별도의 필드로 관리 가능 (선택 사항)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyMenu> weeklyMenus = new ArrayList<>();

    public void toggleIsOpen() {
        isOpen = !isOpen;
    }

    @Builder
    public Store(String imageUrl, String name, String address, String phone, String description, LocalTime openTime, LocalTime closeTime, boolean isOpen, int remain, String hours, double lat, double lng) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
        this.remain = remain;
        this.hours = hours;
        this.lat = lat;
        this.lng = lng;
    }

    public StoreDetailedResponse toDetailedResponse(WeeklyMenuResponse weeklyMenu) {

        return StoreDetailedResponse.builder()
                .id(this.getId())
                .imageUrl(this.getImageUrl())
                .name(this.getName())
                .address(this.getAddress())
                .phone(this.getPhone())
                .description(this.getDescription())
                .openTime(this.getOpenTime())
                .closeTime(this.getCloseTime())
                .isOpen(this.isOpen())
                .remain(this.getRemain())
                .hours(this.getHours())
                .lat(this.getLat())
                .lng(this.getLng())
                .weeklyMenuResponse(weeklyMenu)
                .build();
    }

    public StoreResponse toStoreResponse(DailyMenuDto todayMenu, boolean isHoliday) {
        return StoreResponse.builder()
                .id(this.getId())
                .imageUrl(this.getImageUrl())
                .name(this.getName())
                .address(this.getAddress())
                .phone(this.getPhone())
                .description(this.getDescription())
                .hours(this.getHours())
                .isOpen(this.isOpen())
                .isHoliday(isHoliday)
                .remain(this.getRemain())
                .lat(this.getLat())
                .lng(this.getLng())
                .todayMenu(todayMenu)
                .build();
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
