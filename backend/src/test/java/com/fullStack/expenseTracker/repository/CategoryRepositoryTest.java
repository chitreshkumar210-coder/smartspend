package com.fullStack.expenseTracker.repository;

import com.fullStack.expenseTracker.enums.ETransactionType;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Test
    @DisplayName("existsByCategoryNameAndTransactionType returns true when category exists")
    void existsByCategoryNameAndTransactionType_returnsTrue() {
        TransactionType expense = transactionTypeRepository.save(new TransactionType(ETransactionType.TYPE_EXPENSE));
        categoryRepository.save(new Category("Groceries", expense, true));

        assertTrue(categoryRepository.existsByCategoryNameAndTransactionType("Groceries", expense));
    }

    @Test
    @DisplayName("existsByCategoryNameAndTransactionType returns false when category missing")
    void existsByCategoryNameAndTransactionType_returnsFalse() {
        TransactionType expense = transactionTypeRepository.save(new TransactionType(ETransactionType.TYPE_EXPENSE));

        assertFalse(categoryRepository.existsByCategoryNameAndTransactionType("NonExisting", expense));
    }
}

