package com.hfinance.application.service;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.model.Account;
import com.hfinance.domain.rules.AccountRules;
import com.hfinance.infrastructure.repository.AccountRepository;
import com.hfinance.infrastructure.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountDTO create(String name, String institution, AccountType type, BigDecimal initialBalance) {
        Account account = Account.newAccount(name, institution, type, initialBalance);
        boolean duplicated = name != null && !name.isBlank() && accountRepository.existsActiveByName(name, null);
        AccountRules.validate(account, duplicated);
        return toDTO(accountRepository.save(account));
    }

    public AccountDTO update(Long id, String name, String institution, AccountType type,
                             BigDecimal initialBalance, boolean active) {
        Account existing = getRequired(id);
        Account updated = new Account(id, name, institution, type, initialBalance, active,
                existing.getCreatedAt(), LocalDateTime.now());
        boolean duplicated = name != null && !name.isBlank() && accountRepository.existsActiveByName(name, id);
        AccountRules.validate(updated, duplicated);
        accountRepository.update(updated);
        return toDTO(updated);
    }

    public void deactivate(Long id) {
        getRequired(id);
        accountRepository.deactivate(id);
    }

    public void delete(Long id) {
        getRequired(id);
        if (accountRepository.hasTransactions(id)) {
            throw new BusinessException("Não é possível excluir uma conta que possui transações.");
        }
        accountRepository.delete(id);
    }

    public Account getRequired(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conta não encontrada."));
    }

    public List<AccountDTO> listAccounts() {
        return accountRepository.findAll().stream().map(this::toDTO).toList();
    }

    public BigDecimal calculateCurrentBalance(Account account) {
        return account.getInitialBalance().add(transactionRepository.calculateAccountMovement(account.getId()));
    }

    public BigDecimal calculateTotalBalance() {
        return accountRepository.findAll().stream()
                .filter(Account::isActive)
                .map(this::calculateCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public AccountDTO toDTO(Account account) {
        return AccountDTO.from(account, calculateCurrentBalance(account));
    }
}
