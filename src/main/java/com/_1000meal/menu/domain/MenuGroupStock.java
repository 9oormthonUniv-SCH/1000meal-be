package com._1000meal.menu.domain;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menu_group_stock")
public class MenuGroupStock {

    private static final int LOW_STOCK_THRESHOLD = 10;

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

    @Column(nullable = false)
    private boolean lowStockNotified;

    public static MenuGroupStock of(MenuGroup menuGroup, int capacity) {
        MenuGroupStock s = new MenuGroupStock();
        s.menuGroup = menuGroup;
        s.stock = capacity;
        s.capacity = capacity;
        s.lowStockNotified = false;
        return s;
    }

    /**
     * 재고 차감
     * @param value 차감할 수량
     * @return 품절 임박 알림이 필요한 경우 true (11 초과 -> 10 이하로 떨어지고 아직 미발송인 경우)
     * @throws CustomException 재고 부족 시
     */
    public boolean deduct(int value) {
        if (this.stock < value) {
            throw new CustomException(MenuErrorCode.INSUFFICIENT_STOCK);
        }

        int previousStock = this.stock;
        this.stock -= value;

        // 11 초과에서 10 이하로 떨어지는 순간 + 아직 알림 미발송
        boolean crossedThreshold = previousStock > LOW_STOCK_THRESHOLD
                && this.stock <= LOW_STOCK_THRESHOLD
                && !this.lowStockNotified;

        if (crossedThreshold) {
            this.lowStockNotified = true;
            return true;
        }
        return false;
    }

    /**
     * 재고 직접 수정
     * @param stock 새로운 재고 수량
     */
    public void updateStock(int stock) {
        this.stock = stock;
        // 재고가 threshold 초과로 복구되면 알림 플래그 리셋
        if (stock > LOW_STOCK_THRESHOLD) {
            this.lowStockNotified = false;
        }
    }

    /**
     * 일일 재고 리셋 (capacity로 복구)
     */
    public void resetDaily() {
        this.stock = this.capacity;
        this.lowStockNotified = false;
    }

    /**
     * capacity 변경
     */
    public void updateCapacity(int capacity) {
        this.capacity = capacity;
    }
}
