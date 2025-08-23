package com._1000meal.auth.model;

public record AuthPrincipal(
        Long id,
        String account,
        String name,
        String email,   // admin은 null 가능
        String role     // "STUDENT" | "ADMIN"
) {}