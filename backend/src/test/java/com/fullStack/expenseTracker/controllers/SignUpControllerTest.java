package com.fullStack.expenseTracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullStack.expenseTracker.ExpenseTrackerApplication;
import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignUpController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ExpenseTrackerApplication.class)
@SuppressWarnings({"NullAway", "null"})
class SignUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("removal")
    private AuthService authService;

    @Test
    @DisplayName("POST /signup should delegate to AuthService and return 201")
    void registerUser_shouldReturnCreated() throws Exception {
        SignUpRequestDto requestDto = new SignUpRequestDto("john", "john@example.com", "password123");
        ApiResponseDto<?> responseDto = new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.CREATED, "created");

        when(authService.save(any(SignUpRequestDto.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/mypockit/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).save(any(SignUpRequestDto.class));
    }

    @Test
    @DisplayName("GET /signup/resend should respond with ok when service allows it")
    void resendVerification_shouldReturnOk() throws Exception {
        ApiResponseDto<?> responseDto = new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.OK, "ok");
        when(authService.resendVerificationCode("test@example.com")).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/mypockit/auth/signup/resend").param("email", "test@example.com"))
                .andExpect(status().isOk());

        verify(authService, times(1)).resendVerificationCode("test@example.com");
    }
}

