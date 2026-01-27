package com._1000meal.menu.domain;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menu_group_stock")
public class MenuGroupStock {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int LOW_STOCK_30_THRESHOLD = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_group_id", nullable = false, unique = true)
    private MenuGroup menuGroup;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "last_notified_threshold")
    private Integer lastNotifiedThreshold;

    @Column(name = "last_notified_date")
    private LocalDate lastNotifiedDate;

    public static MenuGroupStock of(MenuGroup menuGroup, int capacity) {
        MenuGroupStock s = new MenuGroupStock();
        s.menuGroup = menuGroup;
        s.stock = capacity;
        s.capacity = capacity;
        s.lastNotifiedThreshold = null;
        s.lastNotifiedDate = null;
        return s;
    }

    /**
     * 재고 차감
     * @param value 차감할 수량
     * @param today 오늘 날짜
     * @return 각 임계치(30, 10)별 알림 발송 필요 여부
     * @throws CustomException 재고 부족 시
     */
    public StockDeductResult deduct(int value, LocalDate today) {
        if (this.stock < value) {
            throw new CustomException(MenuErrorCode.INSUFFICIENT_STOCK);
        }

        if (this.lastNotifiedDate == null || !this.lastNotifiedDate.equals(today)) {
            this.lastNotifiedThreshold = null;
        }

        int previousStock = this.stock;
        this.stock -= value;

        boolean notifiedTodayFor30 = this.lastNotifiedDate != null
                && this.lastNotifiedDate.equals(today)
                && this.lastNotifiedThreshold != null
                && this.lastNotifiedThreshold <= LOW_STOCK_30_THRESHOLD;

        boolean crossedThreshold30 = this.stock <= LOW_STOCK_30_THRESHOLD
                && !notifiedTodayFor30;

        // 11 초과에서 10 이하로 떨어지는 순간 + 10 임계치 알림 미발송
        boolean crossedThreshold10 = previousStock > LOW_STOCK_THRESHOLD
                && this.stock <= LOW_STOCK_THRESHOLD
                && (this.lastNotifiedThreshold == null || this.lastNotifiedThreshold > LOW_STOCK_THRESHOLD);

        // 더 낮은 임계치가 우선 (10이 30보다 우선)
        if (crossedThreshold10) {
            this.lastNotifiedThreshold = LOW_STOCK_THRESHOLD;
            this.lastNotifiedDate = today;
        } else if (crossedThreshold30) {
            this.lastNotifiedThreshold = LOW_STOCK_30_THRESHOLD;
            this.lastNotifiedDate = today;
        }

        return new StockDeductResult(crossedThreshold30, crossedThreshold10);
    }

    /**
     * 재고 직접 수정
     * @param stock 새로운 재고 수량
     */
    public void updateStock(int stock) {
        this.stock = stock;
        if (stock > LOW_STOCK_30_THRESHOLD) {
            // 30 초과로 복구 → 전체 리셋
            this.lastNotifiedThreshold = null;
        } else if (stock > LOW_STOCK_THRESHOLD) {
            // 10 초과 ~ 30 이하 → 10 알림만 리셋 (30 알림 상태는 유지)
            if (this.lastNotifiedThreshold != null && this.lastNotifiedThreshold < LOW_STOCK_30_THRESHOLD) {
                this.lastNotifiedThreshold = LOW_STOCK_30_THRESHOLD;
            }
        }
        // stock <= 10 → 변경 없음
    }

    /**
     * 일일 재고 리셋 (capacity로 복구)
     */
    public void resetDaily() {
        this.stock = this.capacity;
        this.lastNotifiedThreshold = null;
        this.lastNotifiedDate = null;
    }

    /**
     * capacity 변경
     */
    public void updateCapacity(int capacity) {
        this.capacity = capacity;
    }
}
