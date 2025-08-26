package com._1000meal.auth.model;

import com._1000meal.store.domain.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "admin_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_admin_profiles_account", columnNames = "account_id")
)
@Getter
@NoArgsConstructor
public class AdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 1 ↔ 프로필 1
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    // 관리자는 반드시 한 매장에 속한다
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "display_name", length = 50, nullable = false)
    private String displayName;

    @Column(name = "admin_level", nullable = false)
    private int adminLevel; // 1~N (권한 단계)

    /** 팩토리 메서드 */
    public static AdminProfile create(Account account, String displayName, int adminLevel, Store store) {
        AdminProfile p = new AdminProfile();
        p.account = account;
        p.store = store;
        p.displayName = displayName;
        p.adminLevel = adminLevel;
        return p;
    }

    // 변경 메서드
    public void changeDisplayName(String displayName) { this.displayName = displayName; }
    public void changeAdminLevel(int adminLevel)      { this.adminLevel = adminLevel; }
    public void changeStore(Store store)              { this.store = store; }
}