package com.fullStack.expenseTracker.services.impls;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.CategoryRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.CategoryAlreadyExistsException;
import com.fullStack.expenseTracker.exceptions.CategoryNotFoundException;
import com.fullStack.expenseTracker.exceptions.CategoryServiceLogicException;
import com.fullStack.expenseTracker.exceptions.TransactionTypeNotFoundException;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.TransactionType;
import com.fullStack.expenseTracker.repository.CategoryRepository;
import com.fullStack.expenseTracker.services.TransactionTypeService;

/**
 * Example unit test using JUnit 5 and Mockito
 * This demonstrates how to test services with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
@SuppressWarnings({"NullAway", "null"})
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionTypeService transactionTypeService;

    @InjectMocks
    private com.fullStack.expenseTracker.services.impls.CategoryServiceImpl categoryService;

    private final Category testCategory = createTestCategory();

    private Category createTestCategory() {
        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Test Category");
        category.setEnabled(true);
        return category;
    }

    @Test
    @DisplayName("Should add new category when it does not exist already")
    void testAddNewCategory_Success() throws TransactionTypeNotFoundException, CategoryServiceLogicException, CategoryAlreadyExistsException {
        // Arrange
        CategoryRequestDto request = new CategoryRequestDto("Health", 2);
        TransactionType transactionType = new TransactionType();
        when(transactionTypeService.getTransactionById(request.getTransactionTypeId())).thenReturn(transactionType);
        when(categoryRepository.existsByCategoryNameAndTransactionType(request.getCategoryName(), transactionType)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = categoryService.addNewCategory(request);

        // Assert
        assertEquals(org.springframework.http.HttpStatus.CREATED, response.getStatusCode());
        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals("Category has been successfully added!", body.getResponse());
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());
        Category savedCategory = captor.getValue();
        assertNotNull(savedCategory);
    }

    @Test
    @DisplayName("Should throw CategoryAlreadyExistsException when duplicate category is added")
    void testAddNewCategory_whenDuplicate_ThrowsException() throws TransactionTypeNotFoundException {
        // Arrange
        CategoryRequestDto request = new CategoryRequestDto("Health", 2);
        TransactionType transactionType = new TransactionType();
        when(transactionTypeService.getTransactionById(request.getTransactionTypeId())).thenReturn(transactionType);
        when(categoryRepository.existsByCategoryNameAndTransactionType(request.getCategoryName(), transactionType)).thenReturn(true);

        // Act & Assert
        CategoryAlreadyExistsException exception =
                assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.addNewCategory(request));
        assertEquals("Category already exists!", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should toggle category status when enabling or disabling")
    void testEnableOrDisableCategory_TogglesStatus() throws CategoryServiceLogicException, CategoryNotFoundException {
        // Arrange
        Category category = new Category();
        category.setCategoryId(5);
        category.setCategoryName("Utilities");
        category.setEnabled(true);
        when(categoryRepository.findById(category.getCategoryId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        // Act
        var response = categoryService.enableOrDisableCategory(category.getCategoryId());

        // Assert
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        assertFalse(category.isEnabled(), "Category flag should have been toggled");
        verify(categoryRepository, times(1)).save(category);
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

