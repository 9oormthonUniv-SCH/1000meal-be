package com._1000meal.email.dto;

public record EmailStatusResponse(String email, boolean verified, Boolean accountExists) {}
