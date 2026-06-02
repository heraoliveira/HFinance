package com.hfinance.application.service;

import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.BudgetStatus;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Budget;
import com.hfinance.domain.model.Category;
import com.hfinance.domain.rules.BudgetRules;
import com.hfinance.infrastructure.repository.BudgetRepository;
import com.hfinance.infrastructure.repository.CategoryRepository;
import com.hfinance.infrastructure.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public BudgetDTO create(Long categoryId, String name, int month, int year, BigDecimal limitAmount) {
        Budget budget = Budget.newBudget(categoryId, name, month, year, limitAmount);
        validateBudget(budget, null);
        return toDTO(budgetRepository.save(budget));
    }

    public BudgetDTO update(Long id, Long categoryId, String name, int month, int year, BigDecimal limitAmount) {
        Budget existing = getRequired(id);
        Budget updated = new Budget(id, categoryId, name, month, year, limitAmount,
                existing.getCreatedAt(), LocalDateTime.now());
        validateBudget(updated, id);
        budgetRepository.update(updated);
        return toDTO(updated);
    }

    public void delete(Long id) {
        getRequired(id);
        budgetRepository.delete(id);
    }

    public Budget getRequired(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Orçamento não encontrado."));
    }

    public List<BudgetDTO> listBudgets() {
        return budgetRepository.findAll().stream().map(this::toDTO).toList();
    }

    public BudgetDTO toDTO(Budget budget) {
        Category category = categoryRepository.findById(budget.getCategoryId())
                .orElseThrow(() -> new ValidationException("Selecione uma categoria."));
        BigDecimal spent = transactionRepository.sumExpensesByCategoryAndMonth(
                budget.getCategoryId(), budget.getMonth(), budget.getYear());
        BigDecimal percent = BudgetRules.usagePercent(spent, budget.getLimitAmount());
        BudgetStatus status = BudgetRules.calculateStatus(spent, budget.getLimitAmount());
        return BudgetDTO.from(budget, category.getName(), spent, percent, status);
    }

    private void validateBudget(Budget budget, Long excludedId) {
        if (budget.getCategoryId() == null) {
            throw new ValidationException("Selecione uma categoria.");
        }
        Category category = categoryRepository.findById(budget.getCategoryId())
                .orElseThrow(() -> new ValidationException("Selecione uma categoria."));
        boolean duplicated = budgetRepository.existsByCategoryMonthYear(
                budget.getCategoryId(), budget.getMonth(), budget.getYear(), excludedId);
        BudgetRules.validate(budget, duplicated, category.getType() == CategoryType.EXPENSE);
    }
}
