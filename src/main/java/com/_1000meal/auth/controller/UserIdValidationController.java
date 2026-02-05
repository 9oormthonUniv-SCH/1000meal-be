package com._1000meal.auth.controller;

import com._1000meal.auth.dto.UserIdValidateRequest;
import com._1000meal.auth.dto.UserIdValidateResponse;
import com._1000meal.auth.service.UserIdValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Signup - Validation",
        description = "회원가입 사전 검증 API (학번/아이디 중복 및 형식 검사)"
)
@RestController
@RequestMapping("/api/v1/signup/user")
@RequiredArgsConstructor
public class UserIdValidationController {

    private final UserIdValidationService validationService;

    @Operation(
            summary = "학번(ID) 중복 및 형식 검증",
            description = """
                    회원가입 단계에서 사용하는 학번(ID) 검증 API입니다.
                    
                    - 학번(ID)의 형식이 올바른지 검사합니다.
                    - 이미 사용 중인 학번인지 여부를 확인합니다.
                    - 로그인이나 인증 없이 호출할 수 있습니다.
                    
                    ❗ 성공/실패가 아닌, 검증 결과를 Response 값으로 반환합니다.
                    """
    )
    @PostMapping("/validate-id")
    public ResponseEntity<UserIdValidateResponse> validate(
            @RequestBody @Valid UserIdValidateRequest req
    ) {
        UserIdValidateResponse res =
                validationService.validate(req.getUserId());
        return ResponseEntity.ok(res);
    }
}