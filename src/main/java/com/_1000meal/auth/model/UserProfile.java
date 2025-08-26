package com._1000meal.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_profiles_account", columnNames = "account_id")
)
@Getter
@NoArgsConstructor
public class UserProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 1개당 프로필 1개라면 OneToOne 이 자연스럽습니다.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(length = 100) private String department;
    @Column(length = 50,  nullable = false) private String name;
    @Column(length = 20)  private String phone;

    // 팩토리 메서드로 연관관계를 명시적으로 주입
    public static UserProfile create(Account account, String department, String name, String phone) {
        UserProfile p = new UserProfile();
        p.account = account;        // ← 반드시 Account 엔티티를 서비스에서 조회해서 넣어주세요
        p.department = department;
        p.name = name;
        p.phone = phone;
        return p;
    }

    // 변경 메서드들 (선택)
    public void changeDepartment(String department) { this.department = department; }
    public void changeName(String name)             { this.name = name; }
    public void changePhone(String phone)           { this.phone = phone; }
}