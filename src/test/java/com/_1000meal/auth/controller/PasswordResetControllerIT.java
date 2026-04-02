package com._1000meal.auth.controller;

import com._1000meal.auth.service.PasswordResetService;
import com._1000meal.global.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import com._1000meal.global.error.CustomExceptionHandler;
import com._1000meal.global.error.GlobalExceptionHandler;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import({GlobalExceptionHandler.class, CustomExceptionHandler.class})
class PasswordResetControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PasswordResetService passwordResetService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("이메일 형식이 아니면 400 validation error")
    void requestWithInvalidEmailReturnsBadRequest() throws Exception {
        String body = """
                {
                  "email": "20243813"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.result.code").value("COMMON_400_VALIDATION"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));

        verifyNoInteractions(passwordResetService);
    }
}
