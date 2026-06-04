package com.hfinance.ui.controller;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.application.dto.RecurrenceRequest;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.core.format.MoneyFormatter;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.RecurrenceType;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class TransactionController {
    private static TransactionFilterDTO lastFilter = TransactionFilterDTO.empty();

    private final HFinanceContext context;
    private final TableView<TransactionDTO> table = new TableView<>();
    private final ComboBox<AccountDTO> accountCombo = new ComboBox<>();
    private final ComboBox<CategoryDTO> categoryCombo = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final ComboBox<TransactionType> typeCombo = new ComboBox<>();
    private final ComboBox<PaymentMethod> paymentCombo = new ComboBox<>();
    private final TextField descriptionField = UiUtils.textField("Descrição opcional");
    private final TextField amountField = UiUtils.textField("Ex.: 150,00");
    private final ComboBox<RecurrenceType> recurrenceCombo = new ComboBox<>();
    private final TextField recurrenceCountField = UiUtils.textField("Ex.: 12");
    private final DatePicker recurrenceEndPicker = new DatePicker();
    private final Label recurrenceSummary = UiUtils.helperText("Recorrência desativada.");
    private final DatePicker startFilter = new DatePicker();
    private final DatePicker endFilter = new DatePicker();
    private final ComboBox<TransactionType> typeFilter = new ComboBox<>();
    private final ComboBox<PaymentMethod> paymentFilter = new ComboBox<>();
    private final ComboBox<AccountDTO> accountFilter = new ComboBox<>();
    private final ComboBox<CategoryDTO> categoryFilter = new ComboBox<>();
    private final TextField descriptionFilter = UiUtils.textField("Descrição");
    private final TextField minAmountFilter = UiUtils.textField("Valor mínimo");
    private final TextField maxAmountFilter = UiUtils.textField("Valor máximo");
    private final Label activeFiltersLabel = UiUtils.helperText("Nenhum filtro ativo.");
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
        VBox right = new VBox(16,
                UiUtils.panel("Nova transação", form()),
                UiUtils.compactPanel("Leitura rápida", transactionHints()));
        HBox.setHgrow(left, Priority.ALWAYS);
        left.setMinWidth(640);
        right.setPrefWidth(410);
        HBox layout = new HBox(16, left, right);
        root.getChildren().addAll(summaryCards, layout);
        refreshCombos();
        restoreFilters();
        clearForm();
        applyFilters();
        return UiUtils.scrollable(root);
    }

    private void configureTable() {
        if (!table.getColumns().isEmpty()) {
            return;
        }
        table.setPlaceholder(new Label("Nenhuma transação encontrada."));
        table.getColumns().addAll(
                UiUtils.column("Data", item -> UiUtils.date(item.transactionDate())),
                UiUtils.column("Tipo", TransactionDTO::transactionTypeLabel),
                UiUtils.column("Método", TransactionDTO::paymentMethodLabel),
                UiUtils.column("Conta", TransactionDTO::accountName),
                UiUtils.column("Categoria", TransactionDTO::categoryName),
                UiUtils.column("Descrição", TransactionDTO::description),
                UiUtils.column("Recorrência", TransactionDTO::recurrenceLabel),
                UiUtils.column("Valor", item -> UiUtils.money(item.amount()))
        );
        UiUtils.configureTable(table, 390);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> fillForm(value));
    }

    private void configureControls() {
        accountCombo.setConverter(accountConverter());
        accountFilter.setConverter(accountConverter());
        categoryCombo.setConverter(categoryConverter());
        categoryFilter.setConverter(categoryConverter());
        typeCombo.setItems(FXCollections.observableArrayList(Arrays.asList(TransactionType.values())));
        typeFilter.setItems(FXCollections.observableArrayList(Arrays.asList(TransactionType.values())));
        paymentCombo.setItems(FXCollections.observableArrayList(Arrays.asList(PaymentMethod.values())));
        paymentFilter.setItems(FXCollections.observableArrayList(Arrays.asList(PaymentMethod.values())));
        recurrenceCombo.setItems(FXCollections.observableArrayList(Arrays.asList(RecurrenceType.values())));
        typeCombo.valueProperty().addListener((obs, old, value) -> {
            loadCategories(value);
            if (selected == null) {
                categoryCombo.setValue(null);
            }
        });
        recurrenceCombo.valueProperty().addListener((obs, old, value) -> updateRecurrenceState());
        if (typeCombo.getValue() == null) {
            typeCombo.setValue(TransactionType.EXPENSE);
        }
        if (paymentCombo.getValue() == null) {
            paymentCombo.setValue(PaymentMethod.PIX);
        }
        if (recurrenceCombo.getValue() == null) {
            recurrenceCombo.setValue(RecurrenceType.NONE);
        }
    }

    private VBox filters() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Data inicial", startFilter);
        UiUtils.addRow(grid, 1, "Data final", endFilter);
        UiUtils.addRow(grid, 2, "Tipo", typeFilter);
        UiUtils.addRow(grid, 3, "Categoria", categoryFilter);
        UiUtils.addRow(grid, 4, "Método", paymentFilter);
        UiUtils.addRow(grid, 5, "Conta", accountFilter);
        UiUtils.addRow(grid, 6, "Descrição", descriptionFilter);
        UiUtils.addRow(grid, 7, "Valor mínimo", minAmountFilter);
        UiUtils.addRow(grid, 8, "Valor máximo", maxAmountFilter);

        Button currentMonth = UiUtils.secondaryButton("Este mês");
        currentMonth.setOnAction(event -> setCurrentMonthFilter());
        Button lastThirtyDays = UiUtils.secondaryButton("Últimos 30 dias");
        lastThirtyDays.setOnAction(event -> setLastThirtyDaysFilter());
        Button currentYear = UiUtils.secondaryButton("Este ano");
        currentYear.setOnAction(event -> setCurrentYearFilter());
        Button apply = UiUtils.primaryButton("Aplicar filtros");
        apply.setOnAction(event -> applyFilters());
        Button clear = UiUtils.secondaryButton("Limpar filtros");
        clear.setOnAction(event -> clearFilters());
        grid.add(UiUtils.actions(currentMonth, lastThirtyDays, currentYear), 1, 9);
        grid.add(UiUtils.actions(apply, clear), 1, 10);
        return new VBox(10, grid, activeFiltersLabel);
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
        UiUtils.addRow(grid, 7, "Repetir", recurrenceCombo);
        UiUtils.addRow(grid, 8, "Quantidade", recurrenceCountField);
        UiUtils.addRow(grid, 9, "Data final", recurrenceEndPicker);

        Button save = UiUtils.primaryButton("Salvar");
        save.setOnAction(event -> save());
        Button newTransaction = UiUtils.secondaryButton("Nova transação");
        newTransaction.setOnAction(event -> prepareNewBasedOnCurrent());
        Button reset = UiUtils.secondaryButton("Limpar formulário");
        reset.setOnAction(event -> clearForm());
        Button delete = UiUtils.dangerButton("Excluir");
        delete.setOnAction(event -> delete());
        return new VBox(10, grid, recurrenceSummary, UiUtils.actions(save, newTransaction, reset), UiUtils.actions(delete));
    }

    private void save() {
        try {
            Long accountId = accountCombo.getValue() == null ? null : accountCombo.getValue().id();
            Long categoryId = categoryCombo.getValue() == null ? null : categoryCombo.getValue().id();
            BigDecimal amount = MoneyFormatter.parseUserInput(amountField.getText());
            if (selected == null) {
                List<TransactionDTO> created = context.transactionService().createRecurring(accountId, categoryId,
                        datePicker.getValue(), typeCombo.getValue(), paymentCombo.getValue(), descriptionField.getText(),
                        amount, buildRecurrenceRequest());
                Notification.success(created.size() == 1
                        ? "Registro salvo com sucesso."
                        : "%d transações recorrentes foram criadas com sucesso.".formatted(created.size()));
            } else {
                context.transactionService().update(selected.id(), accountId, categoryId, datePicker.getValue(),
                        typeCombo.getValue(), paymentCombo.getValue(), descriptionField.getText(), amount);
                Notification.success("Registro atualizado com sucesso.");
            }
            applyFilters();
            prepareNewBasedOnCurrent();
        } catch (BusinessException | IllegalArgumentException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private RecurrenceRequest buildRecurrenceRequest() {
        RecurrenceType type = recurrenceCombo.getValue() == null ? RecurrenceType.NONE : recurrenceCombo.getValue();
        if (!type.recurring()) {
            return RecurrenceRequest.none();
        }
        Integer repetitions = null;
        if (recurrenceCountField.getText() != null && !recurrenceCountField.getText().isBlank()) {
            try {
                repetitions = Integer.parseInt(recurrenceCountField.getText().trim());
            } catch (NumberFormatException ex) {
                throw new ValidationException("Informe uma quantidade de repetições válida.");
            }
        }
        return new RecurrenceRequest(type, repetitions, recurrenceEndPicker.getValue());
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
        applyFilters();
        clearForm();
    }

    private void applyFilters() {
        try {
            TransactionFilterDTO filter = buildFilter();
            lastFilter = filter;
            List<TransactionDTO> transactions = context.transactionService().filter(filter);
            table.setItems(FXCollections.observableArrayList(transactions));
            UiUtils.adjustTableHeight(table, transactions.size(), 260, 430);
            activeFiltersLabel.setText(activeFilterSummary(filter));
            updateSummary(transactions);
        } catch (BusinessException | IllegalArgumentException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível aplicar os filtros." : ex.getMessage());
        }
    }

    private TransactionFilterDTO buildFilter() {
        return new TransactionFilterDTO(
                startFilter.getValue(),
                endFilter.getValue(),
                null,
                null,
                accountFilter.getValue() == null ? null : accountFilter.getValue().id(),
                categoryFilter.getValue() == null ? null : categoryFilter.getValue().id(),
                typeFilter.getValue(),
                paymentFilter.getValue(),
                descriptionFilter.getText(),
                optionalMoney(minAmountFilter),
                optionalMoney(maxAmountFilter)
        );
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
        categoryFilter.setValue(null);
        descriptionFilter.clear();
        minAmountFilter.clear();
        maxAmountFilter.clear();
        applyFilters();
    }

    private void setCurrentMonthFilter() {
        LocalDate now = LocalDate.now();
        startFilter.setValue(now.withDayOfMonth(1));
        endFilter.setValue(now.withDayOfMonth(now.lengthOfMonth()));
        applyFilters();
    }

    private void setLastThirtyDaysFilter() {
        LocalDate now = LocalDate.now();
        startFilter.setValue(now.minusDays(29));
        endFilter.setValue(now);
        applyFilters();
    }

    private void setCurrentYearFilter() {
        LocalDate now = LocalDate.now();
        startFilter.setValue(LocalDate.of(now.getYear(), 1, 1));
        endFilter.setValue(LocalDate.of(now.getYear(), 12, 31));
        applyFilters();
    }

    private void restoreFilters() {
        startFilter.setValue(lastFilter.startDate());
        endFilter.setValue(lastFilter.endDate());
        typeFilter.setValue(lastFilter.transactionType());
        paymentFilter.setValue(lastFilter.paymentMethod());
        descriptionFilter.setText(lastFilter.description() == null ? "" : lastFilter.description());
        minAmountFilter.setText(lastFilter.minAmount() == null ? "" : lastFilter.minAmount().toPlainString().replace(".", ","));
        maxAmountFilter.setText(lastFilter.maxAmount() == null ? "" : lastFilter.maxAmount().toPlainString().replace(".", ","));
        selectAccount(accountFilter, lastFilter.accountId());
        selectCategory(categoryFilter, lastFilter.categoryId());
    }

    private String activeFilterSummary(TransactionFilterDTO filter) {
        int count = 0;
        count += filter.effectiveStartDate() == null ? 0 : 1;
        count += filter.effectiveEndDate() == null ? 0 : 1;
        count += filter.accountId() == null ? 0 : 1;
        count += filter.categoryId() == null ? 0 : 1;
        count += filter.transactionType() == null ? 0 : 1;
        count += filter.paymentMethod() == null ? 0 : 1;
        count += filter.description() == null || filter.description().isBlank() ? 0 : 1;
        count += filter.minAmount() == null ? 0 : 1;
        count += filter.maxAmount() == null ? 0 : 1;
        if (count == 0) {
            return "Nenhum filtro ativo.";
        }
        return count == 1 ? "1 filtro ativo." : "%d filtros ativos.".formatted(count);
    }

    private void fillForm(TransactionDTO transaction) {
        selected = transaction;
        if (transaction == null) {
            return;
        }
        selectAccount(accountCombo, transaction.accountId());
        typeCombo.setValue(transaction.transactionType());
        loadCategories(transaction.transactionType());
        selectCategory(categoryCombo, transaction.categoryId());
        datePicker.setValue(transaction.transactionDate());
        paymentCombo.setValue(transaction.paymentMethod());
        descriptionField.setText(transaction.description());
        amountField.setText(transaction.amount().toPlainString().replace(".", ","));
        recurrenceCombo.setValue(transaction.recurrenceType());
        recurrenceCountField.setText(transaction.recurrenceTotal() == null ? "" : String.valueOf(transaction.recurrenceTotal()));
        recurrenceEndPicker.setValue(null);
        updateRecurrenceState();
    }

    private void prepareNewBasedOnCurrent() {
        selected = null;
        table.getSelectionModel().clearSelection();
        recurrenceCombo.setValue(RecurrenceType.NONE);
        recurrenceCountField.clear();
        recurrenceEndPicker.setValue(null);
        updateRecurrenceState();
        amountField.requestFocus();
    }

    private void clearForm() {
        selected = null;
        table.getSelectionModel().clearSelection();
        accountCombo.setValue(null);
        typeCombo.setValue(TransactionType.EXPENSE);
        paymentCombo.setValue(PaymentMethod.PIX);
        datePicker.setValue(LocalDate.now());
        descriptionField.clear();
        amountField.clear();
        recurrenceCombo.setValue(RecurrenceType.NONE);
        recurrenceCountField.clear();
        recurrenceEndPicker.setValue(null);
        loadCategories(typeCombo.getValue());
        updateRecurrenceState();
    }

    private void refreshCombos() {
        accountCombo.setItems(FXCollections.observableArrayList(context.accountService().listAccounts()));
        accountFilter.setItems(FXCollections.observableArrayList(context.accountService().listAccounts()));
        categoryFilter.setItems(FXCollections.observableArrayList(context.categoryService().listAll()));
        loadCategories(typeCombo.getValue());
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
                UiUtils.helperText("Use os filtros para reduzir a análise da tabela ao período, categoria ou método desejado."),
                UiUtils.helperText("Nova transação mantém os campos atuais para acelerar cadastros parecidos."),
                UiUtils.helperText("Recorrência gera transações futuras no momento do cadastro e não altera a série ao editar uma ocorrência.")
        );
    }

    private void loadCategories(TransactionType transactionType) {
        if (transactionType == null) {
            categoryCombo.setItems(FXCollections.emptyObservableList());
            return;
        }
        CategoryType categoryType = transactionType == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        categoryCombo.setItems(FXCollections.observableArrayList(context.categoryService().listActiveByType(categoryType)));
    }

    private void updateRecurrenceState() {
        RecurrenceType type = recurrenceCombo.getValue() == null ? RecurrenceType.NONE : recurrenceCombo.getValue();
        boolean editing = selected != null;
        boolean recurring = type.recurring() && !editing;
        recurrenceCountField.setDisable(!recurring);
        recurrenceEndPicker.setDisable(!recurring);
        if (editing && type.recurring()) {
            recurrenceSummary.setText("Esta edição altera somente a ocorrência selecionada.");
        } else if (recurring) {
            recurrenceSummary.setText("Informe quantidade ou data final. O limite seguro é de 120 ocorrências.");
        } else {
            recurrenceSummary.setText("Recorrência desativada.");
        }
    }

    private void selectAccount(ComboBox<AccountDTO> combo, Long accountId) {
        if (accountId == null) {
            combo.setValue(null);
            return;
        }
        combo.getItems().stream().filter(account -> account.id().equals(accountId)).findFirst().ifPresent(combo::setValue);
    }

    private void selectCategory(ComboBox<CategoryDTO> combo, Long categoryId) {
        if (categoryId == null) {
            combo.setValue(null);
            return;
        }
        combo.getItems().stream()
                .filter(category -> category.id().equals(categoryId))
                .findFirst()
                .ifPresentOrElse(combo::setValue, () -> context.categoryService().listAll().stream()
                        .filter(category -> category.id().equals(categoryId))
                        .findFirst()
                        .ifPresent(category -> {
                            combo.getItems().add(category);
                            combo.setValue(category);
                        }));
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
