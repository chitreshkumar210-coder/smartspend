package com.fullStack.expenseTracker.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.CategoryNotFoundException;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.repository.CategoryRepository;
import com.fullStack.expenseTracker.services.impls.CategoryServiceImpl;

/**
 * Example unit test using JUnit 5 and Mockito
 * This demonstrates how to test services with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private final Category testCategory = createTestCategory();

    private Category createTestCategory() {
        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Test Category");
        category.setEnabled(true);
        return category;
    }

    @Test
    @DisplayName("Should return all categories successfully")
    void testGetCategories_Success() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        var response = categoryService.getCategories();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        ApiResponseDto<?> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(ApiResponseStatus.SUCCESS, responseBody.getStatus());
        assertEquals(categories, responseBody.getResponse());

        // Verify mock interactions
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return true when category exists")
    void testExistsCategory_WhenCategoryExists_ReturnsTrue() {
        // Arrange
        int categoryId = 1;
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // Act
        boolean exists = categoryService.existsCategory(categoryId);

        // Assert
        assertTrue(exists);
        verify(categoryRepository, times(1)).existsById(categoryId);
    }

    @Test
    @DisplayName("Should return false when category does not exist")
    void testExistsCategory_WhenCategoryNotExists_ReturnsFalse() {
        // Arrange
        int categoryId = 999;
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // Act
        boolean exists = categoryService.existsCategory(categoryId);

        // Assert
        assertFalse(exists);
        verify(categoryRepository, times(1)).existsById(categoryId);
    }

    @Test
    @DisplayName("Should return category when found by id")
    void testGetCategoryById_WhenCategoryExists_ReturnsCategory() throws CategoryNotFoundException {
        // Arrange
        int categoryId = 1;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // Act
        Category result = categoryService.getCategoryById(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(testCategory.getCategoryId(), result.getCategoryId());
        assertEquals(testCategory.getCategoryName(), result.getCategoryName());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category not found")
    void testGetCategoryById_WhenCategoryNotExists_ThrowsException() {
        // Arrange
        int categoryId = 999;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(categoryId)
        );

        assertEquals("Category not found with id999", exception.getMessage());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Should verify mock was called")
    void testMockVerification() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory));

        // Act
        categoryService.getCategories();

        // Assert - Verify the mock was called exactly once
        verify(categoryRepository, times(1)).findAll();
        
        // Verify the mock was never called with different parameters
        verify(categoryRepository, never()).findById(anyInt());
    }
}

