package com.hfinance.ui.controller;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.application.dto.ReportDataDTO;
import com.hfinance.application.dto.ReportFilterDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.format.MoneyFormatter;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.ui.component.FinancialChartUtils;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.IntStream;

public class ReportController {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    private static ReportFilterDTO lastFilter = ReportFilterDTO.empty();

    private final HFinanceContext context;
    private final TableView<TransactionDTO> table = new TableView<>();
    private final Label summaryLabel = UiUtils.helperText("Gere um relatório para visualizar os totais.");
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private final VBox chartsBox = new VBox(16);
    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();
    private final ComboBox<Integer> monthCombo = new ComboBox<>();
    private final TextField yearField = UiUtils.textField("Ano");
    private final ComboBox<AccountDTO> accountCombo = new ComboBox<>();
    private final ComboBox<CategoryDTO> categoryCombo = new ComboBox<>();
    private final ComboBox<TransactionType> typeCombo = new ComboBox<>();
    private final ComboBox<PaymentMethod> paymentCombo = new ComboBox<>();
    private final TextField minAmountField = UiUtils.textField("Valor mínimo");
    private final TextField maxAmountField = UiUtils.textField("Valor máximo");

    public ReportController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        configureControls();
        configureTable();
        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        HBox layout = new HBox(16);
        VBox filters = UiUtils.panel("Filtros do relatório", filters());
        VBox results = new VBox(16,
                summaryCards,
                UiUtils.compactPanel("Resumo", summaryLabel),
                chartsBox,
                UiUtils.panel("Transações detalhadas", table));
        filters.setPrefWidth(410);
        filters.setMinWidth(360);
        HBox.setHgrow(results, Priority.ALWAYS);
        layout.getChildren().addAll(filters, results);
        root.getChildren().add(layout);
        restoreFilters();
        generate();
        return UiUtils.scrollable(root);
    }

    private void configureControls() {
        accountCombo.setConverter(accountConverter());
        categoryCombo.setConverter(categoryConverter());
        typeCombo.setItems(FXCollections.observableArrayList(Arrays.asList(TransactionType.values())));
        paymentCombo.setItems(FXCollections.observableArrayList(Arrays.asList(PaymentMethod.values())));
        monthCombo.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().toList()));
        accountCombo.setItems(FXCollections.observableArrayList(context.accountService().listAccounts()));
        categoryCombo.setItems(FXCollections.observableArrayList(context.categoryService().listAll()));
    }

    private void configureTable() {
        if (!table.getColumns().isEmpty()) {
            return;
        }
        table.setPlaceholder(new Label("Nenhum dado encontrado para o período selecionado."));
        table.getColumns().addAll(
                UiUtils.column("Data", item -> UiUtils.date(item.transactionDate())),
                UiUtils.column("Tipo", TransactionDTO::transactionTypeLabel),
                UiUtils.column("Método de pagamento", TransactionDTO::paymentMethodLabel),
                UiUtils.column("Conta", TransactionDTO::accountName),
                UiUtils.column("Categoria", TransactionDTO::categoryName),
                UiUtils.column("Descrição", TransactionDTO::description),
                UiUtils.column("Valor", item -> UiUtils.money(item.amount()))
        );
        UiUtils.configureTable(table, 390);
    }

    private VBox filters() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Data inicial", startDate);
        UiUtils.addRow(grid, 1, "Data final", endDate);
        UiUtils.addRow(grid, 2, "Mês", monthCombo);
        UiUtils.addRow(grid, 3, "Ano", yearField);
        UiUtils.addRow(grid, 4, "Conta", accountCombo);
        UiUtils.addRow(grid, 5, "Categoria", categoryCombo);
        UiUtils.addRow(grid, 6, "Tipo", typeCombo);
        UiUtils.addRow(grid, 7, "Método", paymentCombo);
        UiUtils.addRow(grid, 8, "Valor mínimo", minAmountField);
        UiUtils.addRow(grid, 9, "Valor máximo", maxAmountField);

        Button currentMonth = UiUtils.secondaryButton("Este mês");
        currentMonth.setOnAction(event -> setCurrentMonth());
        Button previousMonth = UiUtils.secondaryButton("Mês anterior");
        previousMonth.setOnAction(event -> setPreviousMonth());
        Button lastThirtyDays = UiUtils.secondaryButton("Últimos 30 dias");
        lastThirtyDays.setOnAction(event -> setLastThirtyDays());
        Button currentYear = UiUtils.secondaryButton("Este ano");
        currentYear.setOnAction(event -> setCurrentYear());
        Button previousYear = UiUtils.secondaryButton("Ano anterior");
        previousYear.setOnAction(event -> setPreviousYear());
        Button generate = UiUtils.primaryButton("Gerar relatório");
        generate.setOnAction(event -> generate());
        Button clear = UiUtils.secondaryButton("Limpar filtros");
        clear.setOnAction(event -> clearFilters());
        Button excel = UiUtils.primaryButton("Exportar Excel");
        excel.setOnAction(event -> exportExcel());
        Button csv = UiUtils.secondaryButton("Exportar CSV");
        csv.setOnAction(event -> exportCsv());

        return new VBox(10,
                grid,
                UiUtils.actions(currentMonth, previousMonth),
                UiUtils.actions(lastThirtyDays, currentYear, previousYear),
                UiUtils.actions(generate, clear),
                UiUtils.actions(excel, csv));
    }

    private void generate() {
        try {
            ReportFilterDTO filter = buildFilter();
            lastFilter = filter;
            ReportDataDTO data = context.reportService().generate(filter);
            table.setItems(FXCollections.observableArrayList(data.transactions()));
            UiUtils.adjustTableHeight(table, data.transactions().size(), 260, 430);
            updateSummary(data);
            updateCharts(data);
        } catch (BusinessException | NumberFormatException ex) {
            table.setItems(FXCollections.emptyObservableList());
            UiUtils.adjustTableHeight(table, 0, 260, 430);
            updateEmptySummary(ex.getMessage());
            chartsBox.getChildren().setAll(UiUtils.compactPanel("Gráficos", emptyState("Nenhum gráfico disponível para os filtros aplicados.")));
        }
    }

    private void updateSummary(ReportDataDTO data) {
        BigDecimal periodBalance = data.totalIncome().subtract(data.totalExpense());
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Total de receitas", MoneyFormatter.format(data.totalIncome()), "card-success"),
                UiUtils.summaryCard("Total de despesas", MoneyFormatter.format(data.totalExpense()), "card-danger"),
                UiUtils.summaryCard("Saldo do período", MoneyFormatter.format(periodBalance), "card-gold"),
                UiUtils.summaryCard("Maior despesa", MoneyFormatter.format(largestExpense(data)), "card-danger"),
                UiUtils.summaryCard("Maior categoria", largestExpenseCategory(data), "card-gold"),
                UiUtils.summaryCard("Transações", String.valueOf(data.transactions().size()), "card-accent")
        );
        summaryLabel.setText("Saldo total atual: %s. As exportações usam exatamente os filtros aplicados."
                .formatted(MoneyFormatter.format(data.totalBalance())));
    }

    private void updateEmptySummary(String message) {
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Total de receitas", MoneyFormatter.format(BigDecimal.ZERO), "card-success"),
                UiUtils.summaryCard("Total de despesas", MoneyFormatter.format(BigDecimal.ZERO), "card-danger"),
                UiUtils.summaryCard("Saldo do período", MoneyFormatter.format(BigDecimal.ZERO), "card-gold"),
                UiUtils.summaryCard("Maior despesa", MoneyFormatter.format(BigDecimal.ZERO), "card-danger"),
                UiUtils.summaryCard("Maior categoria", "-", "card-gold"),
                UiUtils.summaryCard("Transações", "0", "card-accent")
        );
        summaryLabel.setText(message == null ? "Não foi possível concluir a operação." : message);
    }

    private void updateCharts(ReportDataDTO data) {
        chartsBox.getChildren().setAll(
                UiUtils.compactPanel("Receitas vs despesas por mês", monthlyChart(data)),
                UiUtils.compactPanel("Despesas por categoria", pieChart(
                        "Despesas por categoria no período",
                        data.expenseByCategory(),
                        "Nenhuma despesa encontrada no período selecionado.")),
                UiUtils.compactPanel("Transações por método de pagamento", pieChart(
                        "Transações por método de pagamento",
                        data.byPaymentMethod(),
                        "Nenhuma transação encontrada no período selecionado."))
        );
    }

    private Node monthlyChart(ReportDataDTO data) {
        if (data.monthlyIncome().isEmpty() && data.monthlyExpense().isEmpty()) {
            return emptyState("Ainda não há dados para este gráfico.");
        }
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        FinancialChartUtils.configureMonthlyBarChart(chart, "Receitas e despesas por mês");
        chart.setMinHeight(250);
        chart.setPrefHeight(270);
        chart.setMaxHeight(300);

        XYChart.Series<String, Number> income = new XYChart.Series<>();
        income.setName("Receitas");
        XYChart.Series<String, Number> expense = new XYChart.Series<>();
        expense.setName("Despesas");
        LinkedHashSet<String> months = new LinkedHashSet<>();
        months.addAll(data.monthlyIncome().keySet());
        months.addAll(data.monthlyExpense().keySet());
        for (String month : months) {
            income.getData().add(new XYChart.Data<>(month, data.monthlyIncome().getOrDefault(month, BigDecimal.ZERO)));
            expense.getData().add(new XYChart.Data<>(month, data.monthlyExpense().getOrDefault(month, BigDecimal.ZERO)));
        }
        chart.getData().addAll(income, expense);
        FinancialChartUtils.installBarTooltips(income);
        FinancialChartUtils.installBarTooltips(expense);
        return chart;
    }

    private Node pieChart(String title, Map<String, BigDecimal> values, String emptyMessage) {
        if (values.isEmpty()) {
            return emptyState(emptyMessage);
        }
        PieChart chart = new PieChart();
        FinancialChartUtils.populatePieChart(chart, title, values);
        chart.setMinHeight(230);
        chart.setPrefHeight(250);
        chart.setMaxHeight(300);
        return chart;
    }

    private Node emptyState(String message) {
        return FinancialChartUtils.emptyState(
                message,
                "Cadastre transações ou ajuste os filtros para visualizar os dados.");
    }

    private BigDecimal largestExpense(ReportDataDTO data) {
        return data.transactions().stream()
                .filter(transaction -> transaction.transactionType() == TransactionType.EXPENSE)
                .map(TransactionDTO::amount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private String largestExpenseCategory(ReportDataDTO data) {
        return data.expenseByCategory().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }

    private void exportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar relatório Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("hfinance-relatorio-%s.xlsx".formatted(FILE_TIMESTAMP.format(LocalDateTime.now())));
        configureExportDirectory(chooser);
        File file = chooser.showSaveDialog(table.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            context.reportService().exportExcel(file.toPath(), buildFilter());
            Notification.success("Relatório exportado com sucesso.");
        } catch (BusinessException | IOException | NumberFormatException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível exportar o relatório." : ex.getMessage());
        }
    }

    private void exportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar transações em CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        chooser.setInitialFileName("hfinance-transacoes-%s.csv".formatted(FILE_TIMESTAMP.format(LocalDateTime.now())));
        configureExportDirectory(chooser);
        File file = chooser.showSaveDialog(table.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            context.reportService().exportCsv(file.toPath(), buildFilter());
            Notification.success("Relatório exportado com sucesso.");
        } catch (BusinessException | IOException | NumberFormatException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível exportar o relatório." : ex.getMessage());
        }
    }

    private ReportFilterDTO buildFilter() {
        Integer year = yearField.getText() == null || yearField.getText().isBlank()
                ? null
                : Integer.parseInt(yearField.getText().trim());
        if (monthCombo.getValue() != null && year == null) {
            year = LocalDate.now().getYear();
        }
        return new ReportFilterDTO(
                startDate.getValue(),
                endDate.getValue(),
                monthCombo.getValue(),
                year,
                accountCombo.getValue() == null ? null : accountCombo.getValue().id(),
                categoryCombo.getValue() == null ? null : categoryCombo.getValue().id(),
                typeCombo.getValue(),
                paymentCombo.getValue(),
                optionalMoney(minAmountField),
                optionalMoney(maxAmountField)
        );
    }

    private BigDecimal optionalMoney(TextField field) {
        return field.getText() == null || field.getText().isBlank() ? null : MoneyFormatter.parseUserInput(field.getText());
    }

    private void configureExportDirectory(FileChooser chooser) {
        try {
            Files.createDirectories(context.appPaths().exportsDirectory());
            chooser.setInitialDirectory(context.appPaths().exportsDirectory().toFile());
        } catch (IOException ex) {
            // Se a pasta padrão não estiver disponível, o FileChooser usa o comportamento nativo.
        }
    }

    private void clearFilters() {
        startDate.setValue(null);
        endDate.setValue(null);
        monthCombo.setValue(null);
        yearField.clear();
        accountCombo.setValue(null);
        categoryCombo.setValue(null);
        typeCombo.setValue(null);
        paymentCombo.setValue(null);
        minAmountField.clear();
        maxAmountField.clear();
        generate();
    }

    private void restoreFilters() {
        startDate.setValue(lastFilter.startDate());
        endDate.setValue(lastFilter.endDate());
        monthCombo.setValue(lastFilter.month());
        yearField.setText(lastFilter.year() == null ? "" : String.valueOf(lastFilter.year()));
        typeCombo.setValue(lastFilter.transactionType());
        paymentCombo.setValue(lastFilter.paymentMethod());
        minAmountField.setText(lastFilter.minAmount() == null ? "" : lastFilter.minAmount().toPlainString().replace(".", ","));
        maxAmountField.setText(lastFilter.maxAmount() == null ? "" : lastFilter.maxAmount().toPlainString().replace(".", ","));
        selectAccount(lastFilter.accountId());
        selectCategory(lastFilter.categoryId());
    }

    private void setCurrentMonth() {
        LocalDate now = LocalDate.now();
        setMonthYear(now.getMonthValue(), now.getYear());
        generate();
    }

    private void setPreviousMonth() {
        LocalDate previous = LocalDate.now().minusMonths(1);
        setMonthYear(previous.getMonthValue(), previous.getYear());
        generate();
    }

    private void setLastThirtyDays() {
        LocalDate now = LocalDate.now();
        monthCombo.setValue(null);
        yearField.clear();
        startDate.setValue(now.minusDays(29));
        endDate.setValue(now);
        generate();
    }

    private void setCurrentYear() {
        setYear(LocalDate.now().getYear());
        generate();
    }

    private void setPreviousYear() {
        setYear(LocalDate.now().minusYears(1).getYear());
        generate();
    }

    private void setMonthYear(int month, int year) {
        startDate.setValue(null);
        endDate.setValue(null);
        monthCombo.setValue(month);
        yearField.setText(String.valueOf(year));
    }

    private void setYear(int year) {
        monthCombo.setValue(null);
        startDate.setValue(LocalDate.of(year, 1, 1));
        endDate.setValue(LocalDate.of(year, 12, 31));
        yearField.setText(String.valueOf(year));
    }

    private void selectAccount(Long accountId) {
        if (accountId == null) {
            accountCombo.setValue(null);
            return;
        }
        accountCombo.getItems().stream().filter(account -> account.id().equals(accountId)).findFirst().ifPresent(accountCombo::setValue);
    }

    private void selectCategory(Long categoryId) {
        if (categoryId == null) {
            categoryCombo.setValue(null);
            return;
        }
        categoryCombo.getItems().stream().filter(category -> category.id().equals(categoryId)).findFirst().ifPresent(categoryCombo::setValue);
    }

    private StringConverter<AccountDTO> accountConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(AccountDTO account) {
                return account == null ? "" : account.name();
            }

            @Override
            public AccountDTO fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<CategoryDTO> categoryConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(CategoryDTO category) {
                return category == null ? "" : category.name();
            }

            @Override
            public CategoryDTO fromString(String string) {
                return null;
            }
        };
    }
}
