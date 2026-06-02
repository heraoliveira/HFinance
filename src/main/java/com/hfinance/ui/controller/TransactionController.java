package com.hfinance.ui.controller;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.format.MoneyFormatter;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TransactionController {
    private final HFinanceContext context;
    private final TableView<TransactionDTO> table = new TableView<>();
    private final ComboBox<AccountDTO> accountCombo = new ComboBox<>();
    private final ComboBox<CategoryDTO> categoryCombo = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final ComboBox<TransactionType> typeCombo = new ComboBox<>();
    private final ComboBox<PaymentMethod> paymentCombo = new ComboBox<>();
    private final TextField descriptionField = UiUtils.textField("Descrição opcional");
    private final TextField amountField = UiUtils.textField("Ex.: 150,00");
    private final DatePicker startFilter = new DatePicker();
    private final DatePicker endFilter = new DatePicker();
    private final ComboBox<TransactionType> typeFilter = new ComboBox<>();
    private final ComboBox<PaymentMethod> paymentFilter = new ComboBox<>();
    private final ComboBox<AccountDTO> accountFilter = new ComboBox<>();
    private final TextField descriptionFilter = UiUtils.textField("Descrição");
    private final TextField minAmountFilter = UiUtils.textField("Valor mínimo");
    private final TextField maxAmountFilter = UiUtils.textField("Valor máximo");
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private TransactionDTO selected;

    public TransactionController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        configureTable();
        configureControls();

        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        VBox left = new VBox(16, UiUtils.panel("Filtros", filters()), UiUtils.panel("Transações", table));
        VBox right = new VBox(16, UiUtils.panel("Nova transação", form()), UiUtils.compactPanel("Leitura rápida", transactionHints()));
        HBox.setHgrow(left, Priority.ALWAYS);
        left.setMinWidth(640);
        right.setPrefWidth(400);
        HBox layout = new HBox(16, left, right);
        root.getChildren().addAll(summaryCards, layout);
        refreshCombos();
        applyFilters();
        return UiUtils.scrollable(root);
    }

    private void configureTable() {
        if (!table.getColumns().isEmpty()) {
            return;
        }
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma transação encontrada."));
        table.getColumns().addAll(
                UiUtils.column("Data", item -> UiUtils.date(item.transactionDate())),
                UiUtils.column("Tipo", TransactionDTO::transactionTypeLabel),
                UiUtils.column("Método", TransactionDTO::paymentMethodLabel),
                UiUtils.column("Conta", TransactionDTO::accountName),
                UiUtils.column("Categoria", TransactionDTO::categoryName),
                UiUtils.column("Descrição", TransactionDTO::description),
                UiUtils.column("Valor", item -> UiUtils.money(item.amount()))
        );
        UiUtils.configureTable(table, 390);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> fillForm(value));
    }

    private void configureControls() {
        accountCombo.setConverter(accountConverter());
        accountFilter.setConverter(accountConverter());
        categoryCombo.setConverter(categoryConverter());
        typeCombo.setItems(FXCollections.observableArrayList(Arrays.asList(TransactionType.values())));
        typeFilter.setItems(FXCollections.observableArrayList(Arrays.asList(TransactionType.values())));
        paymentCombo.setItems(FXCollections.observableArrayList(Arrays.asList(PaymentMethod.values())));
        paymentFilter.setItems(FXCollections.observableArrayList(Arrays.asList(PaymentMethod.values())));
        typeCombo.valueProperty().addListener((obs, old, value) -> loadCategories(value));
        if (typeCombo.getValue() == null) {
            typeCombo.setValue(TransactionType.EXPENSE);
        }
        if (paymentCombo.getValue() == null) {
            paymentCombo.setValue(PaymentMethod.PIX);
        }
    }

    private GridPane filters() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Data inicial", startFilter);
        UiUtils.addRow(grid, 1, "Data final", endFilter);
        UiUtils.addRow(grid, 2, "Tipo", typeFilter);
        UiUtils.addRow(grid, 3, "Método", paymentFilter);
        UiUtils.addRow(grid, 4, "Conta", accountFilter);
        UiUtils.addRow(grid, 5, "Descrição", descriptionFilter);
        UiUtils.addRow(grid, 6, "Valor mínimo", minAmountFilter);
        UiUtils.addRow(grid, 7, "Valor máximo", maxAmountFilter);
        Button apply = UiUtils.primaryButton("Filtrar");
        apply.setOnAction(event -> applyFilters());
        Button clear = UiUtils.secondaryButton("Limpar filtros");
        clear.setOnAction(event -> clearFilters());
        grid.add(UiUtils.actions(apply, clear), 1, 8);
        return grid;
    }

    private VBox form() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Conta", accountCombo);
        UiUtils.addRow(grid, 1, "Tipo", typeCombo);
        UiUtils.addRow(grid, 2, "Categoria", categoryCombo);
        UiUtils.addRow(grid, 3, "Data", datePicker);
        UiUtils.addRow(grid, 4, "Método", paymentCombo);
        UiUtils.addRow(grid, 5, "Descrição", descriptionField);
        UiUtils.addRow(grid, 6, "Valor", amountField);

        Button save = UiUtils.primaryButton("Salvar");
        save.setOnAction(event -> save());
        Button clear = UiUtils.secondaryButton("Nova transação");
        clear.setOnAction(event -> clearForm());
        Button delete = UiUtils.dangerButton("Excluir");
        delete.setOnAction(event -> delete());
        return new VBox(10, grid, UiUtils.actions(save, clear, delete));
    }

    private void save() {
        try {
            Long accountId = accountCombo.getValue() == null ? null : accountCombo.getValue().id();
            Long categoryId = categoryCombo.getValue() == null ? null : categoryCombo.getValue().id();
            if (selected == null) {
                context.transactionService().create(accountId, categoryId, datePicker.getValue(), typeCombo.getValue(),
                        paymentCombo.getValue(), descriptionField.getText(), MoneyFormatter.parseUserInput(amountField.getText()));
                Notification.success("Registro salvo com sucesso.");
            } else {
                context.transactionService().update(selected.id(), accountId, categoryId, datePicker.getValue(), typeCombo.getValue(),
                        paymentCombo.getValue(), descriptionField.getText(), MoneyFormatter.parseUserInput(amountField.getText()));
                Notification.success("Registro atualizado com sucesso.");
            }
            clearForm();
            applyFilters();
        } catch (BusinessException | IllegalArgumentException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) {
            Notification.error("Selecione uma transação.");
            return;
        }
        if (Notification.confirm("Deseja realmente excluir este registro?").orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        context.transactionService().delete(selected.id());
        Notification.success("Registro excluído com sucesso.");
        clearForm();
        applyFilters();
    }

    private void applyFilters() {
        try {
            Long accountId = accountFilter.getValue() == null ? null : accountFilter.getValue().id();
            TransactionFilterDTO filter = new TransactionFilterDTO(
                    startFilter.getValue(),
                    endFilter.getValue(),
                    null,
                    null,
                    accountId,
                    null,
                    typeFilter.getValue(),
                    paymentFilter.getValue(),
                    descriptionFilter.getText(),
                    optionalMoney(minAmountFilter),
                    optionalMoney(maxAmountFilter)
            );
            List<TransactionDTO> transactions = context.transactionService().filter(filter);
            table.setItems(FXCollections.observableArrayList(transactions));
            UiUtils.adjustTableHeight(table, transactions.size(), 260, 430);
            updateSummary(transactions);
        } catch (BusinessException ex) {
            Notification.error(ex.getMessage());
        }
    }

    private BigDecimal optionalMoney(TextField field) {
        return field.getText() == null || field.getText().isBlank() ? null : MoneyFormatter.parseUserInput(field.getText());
    }

    private void clearFilters() {
        startFilter.setValue(null);
        endFilter.setValue(null);
        typeFilter.setValue(null);
        paymentFilter.setValue(null);
        accountFilter.setValue(null);
        descriptionFilter.clear();
        minAmountFilter.clear();
        maxAmountFilter.clear();
        applyFilters();
    }

    private void fillForm(TransactionDTO transaction) {
        selected = transaction;
        if (transaction == null) {
            return;
        }
        selectAccount(accountCombo, transaction.accountId());
        typeCombo.setValue(transaction.transactionType());
        loadCategories(transaction.transactionType());
        selectCategory(transaction.categoryId());
        datePicker.setValue(transaction.transactionDate());
        paymentCombo.setValue(transaction.paymentMethod());
        descriptionField.setText(transaction.description());
        amountField.setText(transaction.amount().toPlainString().replace(".", ","));
    }

    private void clearForm() {
        selected = null;
        table.getSelectionModel().clearSelection();
        accountCombo.setValue(null);
        typeCombo.setValue(TransactionType.EXPENSE);
        paymentCombo.setValue(PaymentMethod.PIX);
        datePicker.setValue(java.time.LocalDate.now());
        descriptionField.clear();
        amountField.clear();
        loadCategories(typeCombo.getValue());
    }

    private void refreshCombos() {
        accountCombo.setItems(FXCollections.observableArrayList(context.accountService().listAccounts()));
        accountFilter.setItems(FXCollections.observableArrayList(context.accountService().listAccounts()));
        loadCategories(typeCombo.getValue());
        clearForm();
    }

    private void updateSummary(List<TransactionDTO> transactions) {
        BigDecimal income = transactions.stream()
                .filter(transaction -> transaction.transactionType() == TransactionType.INCOME)
                .map(TransactionDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = transactions.stream()
                .filter(transaction -> transaction.transactionType() == TransactionType.EXPENSE)
                .map(TransactionDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Transações filtradas", String.valueOf(transactions.size()), "card-accent"),
                UiUtils.summaryCard("Receitas filtradas", UiUtils.money(income), "card-success"),
                UiUtils.summaryCard("Despesas filtradas", UiUtils.money(expense), "card-danger"),
                UiUtils.summaryCard("Saldo filtrado", UiUtils.money(income.subtract(expense)), "card-gold")
        );
    }

    private VBox transactionHints() {
        return new VBox(10,
                UiUtils.helperText("Use os filtros para reduzir a análise da tabela ao período ou método desejado."),
                UiUtils.helperText("Receitas aumentam o saldo da conta e despesas reduzem o saldo automaticamente."),
                UiUtils.helperText("Transações futuras aparecem nos cálculos apenas quando os filtros incluem essas datas.")
        );
    }

    private void loadCategories(TransactionType transactionType) {
        CategoryType categoryType = transactionType == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        categoryCombo.setItems(FXCollections.observableArrayList(context.categoryService().listByType(categoryType)));
    }

    private void selectAccount(ComboBox<AccountDTO> combo, Long accountId) {
        combo.getItems().stream().filter(account -> account.id().equals(accountId)).findFirst().ifPresent(combo::setValue);
    }

    private void selectCategory(Long categoryId) {
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
