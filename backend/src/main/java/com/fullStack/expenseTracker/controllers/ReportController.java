package com.fullStack.expenseTracker.controllers;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mypockit/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/getTotalIncomeOrExpense")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getTotalIncomeOrExpense(@RequestParam("userId") Long userId,
                                                                     @RequestParam("transactionTypeId") int transactionTypeId,
                                                                     @RequestParam("month") int month,
                                                                     @RequestParam("year") int year) {
        return reportService.getTotalByTransactionTypeAndUser(userId, transactionTypeId, month, year);
    }

    @GetMapping("/getTotalNoOfTransactions")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getTotalNoOfTransactions(@RequestParam("userId") Long userId,
                                                                      @RequestParam("month") int month,
                                                                      @RequestParam("year") int year) {
        return reportService.getTotalNoOfTransactionsByUser(userId, month, year);
    }

    @GetMapping("/getTotalByCategory")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getTotalByCategory(@RequestParam("email") String email,
                                                                @RequestParam("categoryId") int categoryId,
                                                                @RequestParam("month") int month,
                                                                @RequestParam("year") int year) {
        return reportService.getTotalExpenseByCategoryAndUser(email, categoryId, month, year);
    }

    @GetMapping("/getMonthlySummaryByUser")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getMonthlySummaryByUser(@RequestParam("email") String email) {
        return reportService.getMonthlySummaryByUser(email);
    }

    @GetMapping("/categoryBreakdown")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> getCategoryBreakdown(@RequestParam("email") String email,
                                                                  @RequestParam("year") int year,
                                                                  @RequestParam("month") int month) {
        return reportService.getCategoryBreakdownByUserAndMonth(email, year, month);
    }
}
