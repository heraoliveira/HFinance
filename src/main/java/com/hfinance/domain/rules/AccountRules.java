package com.hfinance.domain.rules;

import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.model.Account;

public final class AccountRules {
    private AccountRules() {
    }

    public static void validate(Account account, boolean duplicatedActiveName) {
        if (account.getName() == null || account.getName().isBlank()) {
            throw new ValidationException("O nome da conta é obrigatório.");
        }
        if (duplicatedActiveName) {
            throw new ValidationException("Já existe uma conta ativa com este nome.");
        }
        if (account.getInitialBalance() == null) {
            throw new ValidationException("O saldo inicial é obrigatório.");
        }
        if (account.getType() == null) {
            throw new ValidationException("Selecione um tipo de conta.");
        }
        if (account.getInstitution() == null || account.getInstitution().isBlank()) {
            account.setInstitution("Não informado");
        }
    }
}
