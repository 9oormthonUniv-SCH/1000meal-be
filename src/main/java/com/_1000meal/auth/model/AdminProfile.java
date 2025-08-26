package com._1000meal.auth.model;

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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 1 ↔ 프로필 1 이면 OneToOne이 자연스러움
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "display_name", length = 50, nullable = false)
    private String displayName;

    @Column(name = "admin_level", nullable = false)
    private int adminLevel; // 1~N (권한 단계)

    /** 팩토리 메서드 */
    public static AdminProfile create(Account account, String displayName, int adminLevel) {
        AdminProfile p = new AdminProfile();
        p.account = account;
        p.displayName = displayName;
        p.adminLevel = adminLevel;
        return p;
    }

    // 변경 메서드(선택)
    public void changeDisplayName(String displayName) { this.displayName = displayName; }
    public void changeAdminLevel(int adminLevel)      { this.adminLevel = adminLevel; }
}