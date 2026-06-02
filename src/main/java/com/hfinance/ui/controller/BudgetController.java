package com.hfinance.ui.controller;

import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.format.MoneyFormatter;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class BudgetController {
    private final HFinanceContext context;
    private final TableView<BudgetDTO> table = new TableView<>();
    private final TextField nameField = UiUtils.textField("Ex.: Alimentação mensal");
    private final ComboBox<CategoryDTO> categoryCombo = new ComboBox<>();
    private final ComboBox<Integer> monthCombo = new ComboBox<>();
    private final TextField yearField = UiUtils.textField("Ano");
    private final TextField limitField = UiUtils.textField("Ex.: 800,00");
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private BudgetDTO selected;

    public BudgetController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        configureTable();
        configureControls();
        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        HBox layout = new HBox(16);
        VBox tablePanel = UiUtils.panel("Orçamentos", table);
        VBox sidePanel = new VBox(16, UiUtils.panel("Orçamento mensal", form()), UiUtils.compactPanel("Análise dos orçamentos", budgetHints()));
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        tablePanel.setMinWidth(640);
        sidePanel.setPrefWidth(390);
        layout.getChildren().addAll(tablePanel, sidePanel);
        root.getChildren().addAll(summaryCards, layout);
        refresh();
        clear();
        return UiUtils.scrollable(root);
    }

    private void configureTable() {
        if (!table.getColumns().isEmpty()) {
            return;
        }
        table.setPlaceholder(new javafx.scene.control.Label("Nenhum orçamento cadastrado."));
        table.getColumns().addAll(
                UiUtils.column("Nome", BudgetDTO::name),
                UiUtils.column("Categoria", BudgetDTO::categoryName),
                UiUtils.column("Mês", item -> String.valueOf(item.month())),
                UiUtils.column("Ano", item -> String.valueOf(item.year())),
                UiUtils.column("Limite", item -> UiUtils.money(item.limitAmount())),
                UiUtils.column("Gasto", item -> UiUtils.money(item.spentAmount())),
                UiUtils.column("Uso", item -> item.usagePercent() + "%"),
                UiUtils.column("Status", BudgetDTO::statusLabel)
        );
        UiUtils.configureTable(table, 420);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> fill(value));
    }

    private void configureControls() {
        categoryCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoryDTO category) {
                return category == null ? "" : category.name();
            }

            @Override
            public CategoryDTO fromString(String string) {
                return null;
            }
        });
        monthCombo.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().toList()));
    }

    private VBox form() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Nome", nameField);
        UiUtils.addRow(grid, 1, "Categoria", categoryCombo);
        UiUtils.addRow(grid, 2, "Mês", monthCombo);
        UiUtils.addRow(grid, 3, "Ano", yearField);
        UiUtils.addRow(grid, 4, "Valor limite", limitField);

        Button save = UiUtils.primaryButton("Salvar");
        save.setOnAction(event -> save());
        Button clear = UiUtils.secondaryButton("Novo orçamento");
        clear.setOnAction(event -> clear());
        Button delete = UiUtils.dangerButton("Excluir");
        delete.setOnAction(event -> delete());
        return new VBox(10, grid, UiUtils.actions(save, clear, delete));
    }

    private void save() {
        try {
            Long categoryId = categoryCombo.getValue() == null ? null : categoryCombo.getValue().id();
            int month = monthCombo.getValue() == null ? 0 : monthCombo.getValue();
            int year = Integer.parseInt(yearField.getText().trim());
            if (selected == null) {
                context.budgetService().create(categoryId, nameField.getText(), month, year,
                        MoneyFormatter.parseUserInput(limitField.getText()));
                Notification.success("Registro salvo com sucesso.");
            } else {
                context.budgetService().update(selected.id(), categoryId, nameField.getText(), month, year,
                        MoneyFormatter.parseUserInput(limitField.getText()));
                Notification.success("Registro atualizado com sucesso.");
            }
            refresh();
            clear();
        } catch (BusinessException | NumberFormatException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) {
            Notification.error("Selecione um orçamento.");
            return;
        }
        if (Notification.confirm("Deseja realmente excluir este registro?").orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        context.budgetService().delete(selected.id());
        Notification.success("Registro excluído com sucesso.");
        refresh();
        clear();
    }

    private void fill(BudgetDTO budget) {
        selected = budget;
        if (budget == null) {
            return;
        }
        nameField.setText(budget.name());
        categoryCombo.getItems().stream()
                .filter(category -> category.id().equals(budget.categoryId()))
                .findFirst()
                .ifPresent(categoryCombo::setValue);
        monthCombo.setValue(budget.month());
        yearField.setText(String.valueOf(budget.year()));
        limitField.setText(budget.limitAmount().toPlainString().replace(".", ","));
    }

    private void clear() {
        selected = null;
        table.getSelectionModel().clearSelection();
        nameField.clear();
        categoryCombo.setValue(null);
        monthCombo.setValue(LocalDate.now().getMonthValue());
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        limitField.clear();
    }

    private void refresh() {
        categoryCombo.setItems(FXCollections.observableArrayList(context.categoryService().listByType(CategoryType.EXPENSE)));
        List<BudgetDTO> budgets = context.budgetService().listBudgets();
        table.setItems(FXCollections.observableArrayList(budgets));
        UiUtils.adjustTableHeight(table, budgets.size(), 300, 460);
        long onTrack = budgets.stream().filter(budget -> "Dentro do limite".equals(budget.statusLabel())).count();
        long warning = budgets.stream().filter(budget -> "Atenção".equals(budget.statusLabel())).count();
        long exceeded = budgets.stream().filter(budget -> "Excedido".equals(budget.statusLabel())).count();
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Total de orçamentos", String.valueOf(budgets.size()), "card-accent"),
                UiUtils.summaryCard("Dentro do limite", String.valueOf(onTrack), "card-success"),
                UiUtils.summaryCard("Atenção", String.valueOf(warning), "card-gold"),
                UiUtils.summaryCard("Excedidos", String.valueOf(exceeded), "card-danger")
        );
    }

    private VBox budgetHints() {
        return new VBox(10,
                UiUtils.helperText("O valor gasto é calculado pela soma das despesas da categoria no mês."),
                UiUtils.helperText("Atenção começa em 80% de uso e excedido aparece acima de 100%."),
                UiUtils.helperText("Cada categoria só pode ter um orçamento por mês e ano.")
        );
    }
}
