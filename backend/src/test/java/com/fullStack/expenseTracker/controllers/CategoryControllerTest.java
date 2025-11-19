package com.fullStack.expenseTracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullStack.expenseTracker.ExpenseTrackerApplication;
import com.fullStack.expenseTracker.dto.requests.CategoryRequestDto;
import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.services.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ExpenseTrackerApplication.class)
@SuppressWarnings({"NullAway", "null"})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("removal")
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /mypockit/category/getAll should return HTTP 200 for authenticated user")
    @WithMockUser(roles = "USER")
    void shouldGetAllCategories() throws Exception {
        ApiResponseDto<?> responseDto = new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.OK, Collections.emptyList());
        when(categoryService.getCategories()).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/mypockit/category/getAll"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).getCategories();
    }

    @Test
    @DisplayName("POST /mypockit/category/new should create category for admin")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("Food", 1);
        ApiResponseDto<?> responseDto = new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.CREATED, "Category has been successfully added!");
        when(categoryService.addNewCategory(any(CategoryRequestDto.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/mypockit/category/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(categoryService, times(1)).addNewCategory(any(CategoryRequestDto.class));
    }

    @Test
    @DisplayName("DELETE /mypockit/category/delete should toggle a category")
    @WithMockUser(roles = "ADMIN")
    void shouldToggleCategory() throws Exception {
        ApiResponseDto<?> responseDto = new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.OK, "Category has been updated successfully!");
        when(categoryService.enableOrDisableCategory(5)).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/mypockit/category/delete")
                        .param("categoryId", "5"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).enableOrDisableCategory(5);
    }
}

