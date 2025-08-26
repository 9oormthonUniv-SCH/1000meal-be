package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "역할: STUDENT 또는 ADMIN")
        Role role,

        @NotBlank
        @Size(min = 4, max = 30)
        @Schema(description = "학생: 8자리 학번 / 관리자: 로그인 아이디", example = "20250001")
        String userId,

        @NotBlank
        @Size(min = 1, max = 50)
        @Schema(description = "표시 이름", example = "운영자")
        String name,

        @NotBlank
        @Email
        @Schema(description = "학생: 반드시 @sch.ac.kr / 관리자는 일반 메일 허용", example = "user@sch.ac.kr")
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=\\S+$).{8,64}$",
                message = "비밀번호 형식이 올바르지 않습니다."
        )
        @Schema(description = "공백 없음 8~64자", example = "Abcd1234!")
        String password,

        @Schema(description = "관리자 전용: 담당 매장 ID (예: 1=향설1관, 2=야외 그라찌에, 3=베이커리 경)")
        Long storeId
) {}