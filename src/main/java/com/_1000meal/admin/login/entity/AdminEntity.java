package com._1000meal.admin.login.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;   // 로그인용 아이디

    @Column(nullable = false)
    private String password;   // 암호화된 비밀번호

    @Column(nullable = false)
    private String name;       // 관리자 이름

    @Column(nullable = false)
    private String phoneNumber;

    // 추가 예정
}