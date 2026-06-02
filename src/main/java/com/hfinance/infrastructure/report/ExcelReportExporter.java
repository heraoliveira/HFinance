package com.hfinance.infrastructure.report;

import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.application.dto.ReportDataDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.format.DateFormatter;
import com.hfinance.core.format.MoneyFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ExcelReportExporter {
    public void export(Path outputPath, ReportDataDTO data) throws IOException {
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            createSummarySheet(workbook, headerStyle, data);
            createTransactionsSheet(workbook, headerStyle, data);
            createMapSheet(workbook, headerStyle, "Receitas", "Mês", "Valor", data.monthlyIncome());
            createMapSheet(workbook, headerStyle, "Despesas", "Mês", "Valor", data.monthlyExpense());
            createCategorySheet(workbook, headerStyle, data);
            createMapSheet(workbook, headerStyle, "Por Conta", "Conta", "Saldo", data.balanceByAccount());
            createMapSheet(workbook, headerStyle, "Por Método de Pagamento", "Método de pagamento", "Valor", data.byPaymentMethod());
            createBudgetSheet(workbook, headerStyle, data);
            createGoalSheet(workbook, headerStyle, data);

            try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
                workbook.write(outputStream);
            }
        }
    }

    private void createSummarySheet(Workbook workbook, CellStyle headerStyle, ReportDataDTO data) {
        Sheet sheet = workbook.createSheet("Resumo");
        writeHeader(sheet, headerStyle, "Indicador", "Valor");
        writeRow(sheet, 1, "Receitas", MoneyFormatter.format(data.totalIncome()));
        writeRow(sheet, 2, "Despesas", MoneyFormatter.format(data.totalExpense()));
        writeRow(sheet, 3, "Saldo do período", MoneyFormatter.format(data.totalIncome().subtract(data.totalExpense())));
        writeRow(sheet, 4, "Saldo total", MoneyFormatter.format(data.totalBalance()));
        autosize(sheet, 2);
    }

    private void createTransactionsSheet(Workbook workbook, CellStyle headerStyle, ReportDataDTO data) {
        Sheet sheet = workbook.createSheet("Transações");
        writeHeader(sheet, headerStyle, "Data", "Tipo", "Método de pagamento", "Conta", "Categoria", "Descrição", "Valor");
        int rowIndex = 1;
        for (TransactionDTO transaction : data.transactions()) {
            writeRow(sheet, rowIndex++,
                    DateFormatter.format(transaction.transactionDate()),
                    transaction.transactionTypeLabel(),
                    transaction.paymentMethodLabel(),
                    transaction.accountName(),
                    transaction.categoryName(),
                    transaction.description(),
                    MoneyFormatter.format(transaction.amount()));
        }
        autosize(sheet, 7);
    }

    private void createMapSheet(Workbook workbook, CellStyle headerStyle, String name, String firstHeader,
                                String secondHeader, Map<String, BigDecimal> values) {
        Sheet sheet = workbook.createSheet(name);
        writeHeader(sheet, headerStyle, firstHeader, secondHeader);
        int rowIndex = 1;
        for (Map.Entry<String, BigDecimal> entry : values.entrySet()) {
            writeRow(sheet, rowIndex++, entry.getKey(), MoneyFormatter.format(entry.getValue()));
        }
        autosize(sheet, 2);
    }

    private void createCategorySheet(Workbook workbook, CellStyle headerStyle, ReportDataDTO data) {
        Sheet sheet = workbook.createSheet("Por Categoria");
        writeHeader(sheet, headerStyle, "Tipo", "Categoria", "Valor");
        int rowIndex = 1;
        for (Map.Entry<String, BigDecimal> entry : data.incomeByCategory().entrySet()) {
            writeRow(sheet, rowIndex++, "Receita", entry.getKey(), MoneyFormatter.format(entry.getValue()));
        }
        for (Map.Entry<String, BigDecimal> entry : data.expenseByCategory().entrySet()) {
            writeRow(sheet, rowIndex++, "Despesa", entry.getKey(), MoneyFormatter.format(entry.getValue()));
        }
        autosize(sheet, 3);
    }

    private void createBudgetSheet(Workbook workbook, CellStyle headerStyle, ReportDataDTO data) {
        Sheet sheet = workbook.createSheet("Orçamentos");
        writeHeader(sheet, headerStyle, "Nome", "Categoria", "Mês", "Ano", "Limite", "Gasto", "Uso", "Status");
        int rowIndex = 1;
        for (BudgetDTO budget : data.budgets()) {
            writeRow(sheet, rowIndex++,
                    budget.name(),
                    budget.categoryName(),
                    String.valueOf(budget.month()),
                    String.valueOf(budget.year()),
                    MoneyFormatter.format(budget.limitAmount()),
                    MoneyFormatter.format(budget.spentAmount()),
                    budget.usagePercent() + "%",
                    budget.statusLabel());
        }
        autosize(sheet, 8);
    }

    private void createGoalSheet(Workbook workbook, CellStyle headerStyle, ReportDataDTO data) {
        Sheet sheet = workbook.createSheet("Metas");
        writeHeader(sheet, headerStyle, "Nome", "Conta", "Valor alvo", "Valor atual", "Prazo", "Progresso", "Status");
        int rowIndex = 1;
        for (GoalDTO goal : data.goals()) {
            writeRow(sheet, rowIndex++,
                    goal.name(),
                    goal.accountName(),
                    MoneyFormatter.format(goal.targetAmount()),
                    MoneyFormatter.format(goal.currentAmount()),
                    DateFormatter.format(goal.deadline()),
                    goal.progressPercent() + "%",
                    goal.statusLabel());
        }
        autosize(sheet, 7);
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void writeHeader(Sheet sheet, CellStyle headerStyle, String... values) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeRow(Sheet sheet, int rowIndex, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private void autosize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
