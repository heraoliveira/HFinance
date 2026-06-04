package com.hfinance.infrastructure.report;

import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.format.DateFormatter;
import com.hfinance.core.format.MoneyFormatter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class CsvTransactionExporter {
    private static final String SEPARATOR = ";";

    public void export(Path outputPath, List<TransactionDTO> transactions) throws IOException {
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<String> rows = transactions.stream().map(this::row).collect(Collectors.toList());
        rows.add(0, "Data;Tipo;Método de pagamento;Conta;Categoria;Descrição;Valor");
        Files.write(outputPath, rows, StandardCharsets.UTF_8);
    }

    private String row(TransactionDTO transaction) {
        return String.join(SEPARATOR,
                escape(DateFormatter.format(transaction.transactionDate())),
                escape(transaction.transactionTypeLabel()),
                escape(transaction.paymentMethodLabel()),
                escape(transaction.accountName()),
                escape(transaction.categoryName()),
                escape(transaction.description()),
                escape(MoneyFormatter.format(transaction.amount()))
        );
    }

    private String escape(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(SEPARATOR) || safeValue.contains("\"")
                || safeValue.contains("\n") || safeValue.contains("\r")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }
}
