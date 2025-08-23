package com._1000meal.auth.model;

import com._1000meal.global.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "accounts")
@Getter @NoArgsConstructor
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true) private String username;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String passwordHash;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Role role; // STUDENT/ADMIN

    @Column(nullable=false)
    private String status; // PENDING/ACTIVE/SUSPENDED

    public Account(Long id, String username, String email, String passwordHash, Role role, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    // 상태 전이 등 명시적 메서드로 (setter 지양)
    public void activate() { this.status = "ACTIVE"; }
}