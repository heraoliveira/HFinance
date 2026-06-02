package com.hfinance.application.service;

import com.hfinance.application.dto.GoalDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.model.Account;
import com.hfinance.domain.model.Goal;
import com.hfinance.domain.rules.GoalRules;
import com.hfinance.infrastructure.repository.AccountRepository;
import com.hfinance.infrastructure.repository.GoalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GoalService {
    private final GoalRepository goalRepository;
    private final AccountRepository accountRepository;

    public GoalService(GoalRepository goalRepository, AccountRepository accountRepository) {
        this.goalRepository = goalRepository;
        this.accountRepository = accountRepository;
    }

    public GoalDTO create(Long accountId, String name, BigDecimal targetAmount, BigDecimal currentAmount,
                          LocalDate deadline) {
        validateOptionalAccount(accountId);
        Goal goal = Goal.newGoal(accountId, name, targetAmount, currentAmount, deadline);
        GoalRules.validate(goal);
        goal.setStatus(GoalRules.calculateStatus(goal, LocalDate.now()));
        return toDTO(goalRepository.save(goal));
    }

    public GoalDTO update(Long id, Long accountId, String name, BigDecimal targetAmount, BigDecimal currentAmount,
                          LocalDate deadline) {
        validateOptionalAccount(accountId);
        Goal existing = getRequired(id);
        Goal goal = new Goal(id, accountId, name, targetAmount, currentAmount, deadline, existing.getStatus(),
                existing.getCreatedAt(), LocalDateTime.now());
        GoalRules.validate(goal);
        goal.setStatus(GoalRules.calculateStatus(goal, LocalDate.now()));
        goalRepository.update(goal);
        return toDTO(goal);
    }

    public void delete(Long id) {
        getRequired(id);
        goalRepository.delete(id);
    }

    public Goal getRequired(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Meta não encontrada."));
    }

    public List<GoalDTO> listGoals() {
        return goalRepository.findAll().stream().map(goal -> {
            goal.setStatus(GoalRules.calculateStatus(goal, LocalDate.now()));
            return toDTO(goal);
        }).toList();
    }

    public GoalDTO toDTO(Goal goal) {
        BigDecimal progress = GoalRules.progressPercent(goal.getCurrentAmount(), goal.getTargetAmount());
        String accountName = goal.getAccountId() == null
                ? "Sem conta vinculada"
                : accountRepository.findById(goal.getAccountId()).map(Account::getName).orElse("Conta removida");
        return GoalDTO.from(goal, accountName, progress);
    }

    private void validateOptionalAccount(Long accountId) {
        if (accountId != null && accountRepository.findById(accountId).isEmpty()) {
            throw new ValidationException("Selecione uma conta.");
        }
    }
}
