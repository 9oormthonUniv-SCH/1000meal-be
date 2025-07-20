package com._1000meal.store.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // 매장 이름
    private String address;       // 위치주소
    private String phone;         // 전화번호
    private String description;   // 간단한 설명

    private LocalTime openTime;   // 영업시작시간
    private LocalTime closeTime;  // 영업종료시간

    private int remain;           // 남은 수량
    private String hours;         // 운영 시간 문자열 ("08:00 ~ 소진 시")

    private double lat;           // 위도
    private double lng;           // 경도

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Menu> menus = new ArrayList<>(); // 해당 식당의 메뉴들
}
