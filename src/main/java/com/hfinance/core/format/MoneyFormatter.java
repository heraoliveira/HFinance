package com.hfinance.core.format;

import com.hfinance.core.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {
    public static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");

    private MoneyFormatter() {
    }

    public static String format(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(BRAZIL).format(safeValue);
    }

    public static BigDecimal parseUserInput(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ValidationException("O valor deve ser informado.");
        }
        try {
            String normalized = rawValue.trim()
                    .replace("R$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Informe um valor monetário válido.");
        }
    }
}
