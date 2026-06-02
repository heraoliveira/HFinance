package com.hfinance.infrastructure.repository;

import com.hfinance.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    void update(Account account);
    Optional<Account> findById(Long id);
    List<Account> findAll();
    boolean existsActiveByName(String name, Long excludedId);
    boolean hasTransactions(Long accountId);
    void deactivate(Long accountId);
    void delete(Long accountId);
}
