package com.hfinance.infrastructure;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.ReportDataDTO;
import com.hfinance.application.dto.ReportFilterDTO;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Category;
import com.hfinance.testsupport.TestSupport;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportTest {
    @TempDir
    Path tempDir;

    @Test
    void generatesReportTotalsAndExportsExcelWithRequiredPortugueseSheets() throws Exception {
        TestSupport.Fixture fixture = filledFixture();
        ReportDataDTO data = fixture.reportService().generate(ReportFilterDTO.empty());

        assertThat(data.totalIncome()).isEqualByComparingTo("500.00");
        assertThat(data.totalExpense()).isEqualByComparingTo("150.00");

        Path output = tempDir.resolve("relatorio.xlsx");
        fixture.reportService().exportExcel(output, ReportFilterDTO.empty());

        try (Workbook workbook = WorkbookFactory.create(output.toFile())) {
            assertThat(workbook.getSheet("Resumo")).isNotNull();
            assertThat(workbook.getSheet("Transações")).isNotNull();
            assertThat(workbook.getSheet("Receitas")).isNotNull();
            assertThat(workbook.getSheet("Despesas")).isNotNull();
            assertThat(workbook.getSheet("Por Categoria")).isNotNull();
            assertThat(workbook.getSheet("Por Conta")).isNotNull();
            assertThat(workbook.getSheet("Por Método de Pagamento")).isNotNull();
            assertThat(workbook.getSheet("Orçamentos")).isNotNull();
            assertThat(workbook.getSheet("Metas")).isNotNull();
            XSSFSheet transactions = (XSSFSheet) workbook.getSheet("Transações");
            assertThat(transactions.getPaneInformation()).isNotNull();
            assertThat(transactions.getCTWorksheet().isSetAutoFilter()).isTrue();
        }
    }

    @Test
    void exportsCsvWithPortugueseHeaders() throws Exception {
        TestSupport.Fixture fixture = filledFixture();
        Path output = tempDir.resolve("transacoes.csv");

        fixture.reportService().exportCsv(output, ReportFilterDTO.empty());

        String header = Files.readAllLines(output, StandardCharsets.UTF_8).get(0);
        assertThat(header).isEqualTo("Data;Tipo;Método de pagamento;Conta;Categoria;Descrição;Valor");
    }

    @Test
    void exportsCsvRespectingFiltersAndEscapingSpecialCharacters() throws Exception {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("1000.00"));
        Category expense = fixture.expenseCategory();
        fixture.transactionService().create(account.id(), expense.getId(), LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.PIX, "Linha 1\r\nLinha; \"2\"", new BigDecimal("42.00"));
        fixture.transactionService().create(account.id(), expense.getId(), LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.CASH, "Dinheiro", new BigDecimal("10.00"));
        Path output = tempDir.resolve("transacoes-filtradas.csv");

        fixture.reportService().exportCsv(output, new ReportFilterDTO(null, null, null, null,
                account.id(), expense.getId(), TransactionType.EXPENSE, PaymentMethod.PIX, null, null));

        String csv = Files.readString(output, StandardCharsets.UTF_8);
        assertThat(csv).contains("Método de pagamento");
        assertThat(csv).contains("\"Linha 1\r\nLinha; \"\"2\"\"\"");
        assertThat(csv).doesNotContain("Dinheiro");
    }

    @Test
    void reportFiltersCanIncludeFutureTransactionsOnlyWhenDateMatches() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("1000.00"));
        Category income = fixture.incomeCategory();
        fixture.transactionService().create(account.id(), income.getId(), LocalDate.of(2026, 7, 10),
                TransactionType.INCOME, PaymentMethod.PIX, "Receita futura", new BigDecimal("700.00"));

        assertThatThrownBy(() -> fixture.reportService().generate(new ReportFilterDTO(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                null, null, null, null, null, null, null, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Nenhum dado encontrado para o período selecionado.");
        ReportDataDTO data = fixture.reportService().generate(new ReportFilterDTO(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31),
                null, null, null, null, null, null, null, null));
        assertThat(data.totalIncome()).isEqualByComparingTo("700.00");
    }

    @Test
    void rejectsEmptyReport() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);

        assertThatThrownBy(() -> fixture.reportService().generate(ReportFilterDTO.empty()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Nenhum dado encontrado para o período selecionado.");
    }

    private TestSupport.Fixture filledFixture() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("1000.00"));
        Category income = fixture.incomeCategory();
        Category expense = fixture.expenseCategory();
        LocalDate today = LocalDate.now();
        fixture.transactionService().create(account.id(), income.getId(), today, TransactionType.INCOME,
                PaymentMethod.PIX, "Receita", new BigDecimal("500.00"));
        fixture.transactionService().create(account.id(), expense.getId(), today, TransactionType.EXPENSE,
                PaymentMethod.CREDIT_CARD, "Despesa", new BigDecimal("150.00"));
        fixture.budgetService().create(expense.getId(), "Orçamento", today.getMonthValue(), today.getYear(),
                new BigDecimal("300.00"));
        fixture.goalService().create(account.id(), "Meta", new BigDecimal("1000.00"),
                new BigDecimal("300.00"), today.plusMonths(2));
        return fixture;
    }
}
