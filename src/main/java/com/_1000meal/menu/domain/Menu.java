package com._1000meal.menu.domain;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;  // 해당 식당 (FK -> Store)

    private LocalDate date;       // 제공날짜
    private String imageUrl;      // 메뉴 이미지
}
