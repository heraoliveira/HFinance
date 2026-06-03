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
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
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
import java.util.Arrays;
import java.util.stream.IntStream;

public class ReportController {
    private final HFinanceContext context;
    private final TableView<TransactionDTO> table = new TableView<>();
    private final Label summaryLabel = UiUtils.helperText("Gere um relatório para visualizar os totais.");
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();
    private final ComboBox<Integer> monthCombo = new ComboBox<>();
    private final TextField yearField = UiUtils.textField("Ano");
    private final ComboBox<AccountDTO> accountCombo = new ComboBox<>();
    private final ComboBox<CategoryDTO> categoryCombo = new ComboBox<>();
    private final ComboBox<TransactionType> typeCombo = new ComboBox<>();
    private final ComboBox<PaymentMethod> paymentCombo = new ComboBox<>();

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
        VBox results = new VBox(16, summaryCards, UiUtils.compactPanel("Resumo", summaryLabel), UiUtils.panel("Transações", table));
        filters.setPrefWidth(390);
        filters.setMinWidth(340);
        HBox.setHgrow(results, Priority.ALWAYS);
        layout.getChildren().addAll(filters, results);
        root.getChildren().add(layout);
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
        UiUtils.addRow(grid, 7, "Método de pagamento", paymentCombo);

        Button generate = UiUtils.primaryButton("Gerar relatório");
        generate.setOnAction(event -> generate());
        Button clear = UiUtils.secondaryButton("Limpar filtros");
        clear.setOnAction(event -> clearFilters());
        Button excel = UiUtils.primaryButton("Exportar Excel");
        excel.setOnAction(event -> exportExcel());
        Button csv = UiUtils.secondaryButton("Exportar CSV");
        csv.setOnAction(event -> exportCsv());

        return new VBox(10, grid, UiUtils.actions(generate, clear), UiUtils.actions(excel, csv));
    }

    private void generate() {
        try {
            ReportDataDTO data = context.reportService().generate(buildFilter());
            table.setItems(FXCollections.observableArrayList(data.transactions()));
            UiUtils.adjustTableHeight(table, data.transactions().size(), 260, 430);
            BigDecimal periodBalance = data.totalIncome().subtract(data.totalExpense());
            summaryCards.getChildren().setAll(
                    UiUtils.summaryCard("Receitas", MoneyFormatter.format(data.totalIncome()), "card-success"),
                    UiUtils.summaryCard("Despesas", MoneyFormatter.format(data.totalExpense()), "card-danger"),
                    UiUtils.summaryCard("Saldo do período", MoneyFormatter.format(periodBalance), "card-gold"),
                    UiUtils.summaryCard("Transações", String.valueOf(data.transactions().size()), "card-accent")
            );
            summaryLabel.setText("Saldo total atual: %s. Use os botões de exportação para gerar Excel ou CSV com os filtros aplicados."
                    .formatted(MoneyFormatter.format(data.totalBalance())));
        } catch (BusinessException | NumberFormatException ex) {
            table.setItems(FXCollections.emptyObservableList());
            UiUtils.adjustTableHeight(table, 0, 260, 430);
            summaryCards.getChildren().setAll(
                    UiUtils.summaryCard("Receitas", MoneyFormatter.format(BigDecimal.ZERO), "card-success"),
                    UiUtils.summaryCard("Despesas", MoneyFormatter.format(BigDecimal.ZERO), "card-danger"),
                    UiUtils.summaryCard("Saldo do período", MoneyFormatter.format(BigDecimal.ZERO), "card-gold"),
                    UiUtils.summaryCard("Transações", "0", "card-accent")
            );
            summaryLabel.setText(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void exportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar relatório Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("relatorio-hfinance.xlsx");
        configureExportDirectory(chooser);
        File file = chooser.showSaveDialog(table.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            context.reportService().exportExcel(file.toPath(), buildFilter());
            Notification.success("Relatório exportado com sucesso.");
        } catch (BusinessException | IOException | NumberFormatException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void exportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar transações em CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        chooser.setInitialFileName("transacoes-hfinance.csv");
        configureExportDirectory(chooser);
        File file = chooser.showSaveDialog(table.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            context.reportService().exportCsv(file.toPath(), buildFilter());
            Notification.success("Relatório exportado com sucesso.");
        } catch (BusinessException | IOException | NumberFormatException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
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
                null,
                null
        );
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
        generate();
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
