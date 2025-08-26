package com._1000meal.auth.model;

import com._1000meal.global.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Getter @NoArgsConstructor
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학번/아이디 — DB의 username 컬럼에 매핑
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT / ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status; // ACTIVE, PENDING, SUSPENDED ...

    public Account(Long id, String userId, String email, String passwordHash, Role role, AccountStatus status) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public void activate() { this.status = AccountStatus.ACTIVE; }
}