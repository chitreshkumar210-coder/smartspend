package com.fullStack.expenseTracker.dataSeeders;

import com.fullStack.expenseTracker.enums.ETransactionType;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.TransactionType;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.CategoryRepository;
import com.fullStack.expenseTracker.repository.RoleRepository;
import com.fullStack.expenseTracker.repository.TransactionTypeRepository;
import com.fullStack.expenseTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component
public class AdminAndCategoryDataSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener
    @Transactional
    public void loadAdminAndCategories(ContextRefreshedEvent event) {
        // Check if admin exists and delete if password might be wrong
        if (userRepository.existsByEmail("admin@gmail.com")) {
            System.out.println("Admin user already exists. Checking if recreation is needed...");
            // Optionally delete and recreate to ensure correct password
            // Uncomment the next 3 lines to force recreate admin user:
            // User existingAdmin = userRepository.findByEmail("admin@gmail.com").orElse(null);
            // if (existingAdmin != null) {
            //     userRepository.delete(existingAdmin);
            //     System.out.println("Deleted existing admin user to recreate with correct password.");
            // } else {
            //     return;
            // }
            // For now, skip if exists
            return;
        }

        System.out.println("Creating admin user and categories...");

        // Get or create roles
        Role adminRole = roleRepository.findByName(com.fullStack.expenseTracker.enums.ERole.ROLE_ADMIN)
                .orElseGet(() -> {
                    System.out.println("ROLE_ADMIN not found. Creating it...");
                    Role newRole = new Role(com.fullStack.expenseTracker.enums.ERole.ROLE_ADMIN);
                    return roleRepository.save(newRole);
                });
        
        // Ensure ROLE_USER exists as well
        roleRepository.findByName(com.fullStack.expenseTracker.enums.ERole.ROLE_USER)
                .orElseGet(() -> {
                    System.out.println("ROLE_USER not found. Creating it...");
                    Role newRole = new Role(com.fullStack.expenseTracker.enums.ERole.ROLE_USER);
                    return roleRepository.save(newRole);
                });

        // Create admin user
        User.UserBuilder adminBuilder = User.builder()
                .username("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("admin@123"))
                .enabled(true)
                .roles(new HashSet<>(Arrays.asList(adminRole)));
        
        final User admin = Objects.requireNonNull(
                adminBuilder.build(),
                "Failed to create admin user");

        userRepository.save(admin);
        System.out.println("Admin user created successfully: admin@gmail.com");

        // Get transaction types
        TransactionType expenseType = transactionTypeRepository.findByTransactionTypeName(ETransactionType.TYPE_EXPENSE);
        if (expenseType == null) {
            throw new RuntimeException("TYPE_EXPENSE not found. Make sure TransactionTypeDataSeeder runs first.");
        }

        TransactionType incomeType = transactionTypeRepository.findByTransactionTypeName(ETransactionType.TYPE_INCOME);
        if (incomeType == null) {
            throw new RuntimeException("TYPE_INCOME not found. Make sure TransactionTypeDataSeeder runs first.");
        }

        // Expense Categories
        List<String> expenseCategories = Arrays.asList(
                "Food & Dining",
                "Transportation",
                "Shopping",
                "Bills & Utilities",
                "Entertainment",
                "Healthcare",
                "Education",
                "Travel",
                "Personal Care",
                "Gifts & Donations"
        );

        for (String categoryName : expenseCategories) {
            if (!categoryRepository.existsByCategoryNameAndTransactionType(categoryName, expenseType)) {
                categoryRepository.save(new Category(categoryName, expenseType, true));
            }
        }

        // Income Categories
        List<String> incomeCategories = Arrays.asList(
                "Salary",
                "Freelance",
                "Investment",
                "Business",
                "Rental Income",
                "Bonus",
                "Gift Received",
                "Other Income"
        );

        for (String categoryName : incomeCategories) {
            if (!categoryRepository.existsByCategoryNameAndTransactionType(categoryName, incomeType)) {
                categoryRepository.save(new Category(categoryName, incomeType, true));
            }
        }

        System.out.println("Admin and categories setup completed successfully!");
    }
}

