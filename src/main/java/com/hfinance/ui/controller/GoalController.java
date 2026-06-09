package com.hfinance.ui.controller;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.format.MoneyFormatter;
import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

import java.time.LocalDate;
import java.util.List;

public class GoalController {
    private final HFinanceContext context;
    private final TableView<GoalDTO> table = new TableView<>();
    private final TextField nameField = UiUtils.textField("Ex.: Reserva de emergência");
    private final TextField targetField = UiUtils.textField("Valor alvo");
    private final TextField currentField = UiUtils.textField("Valor atual");
    private final DatePicker deadlinePicker = new DatePicker();
    private final ComboBox<AccountDTO> accountCombo = new ComboBox<>();
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private GoalDTO selected;

    public GoalController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        configureTable();
        configureControls();
        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        HBox layout = new HBox(16);
        VBox tablePanel = UiUtils.panel("Metas financeiras", table);
        VBox sidePanel = new VBox(16, UiUtils.panel("Meta", form()), UiUtils.compactPanel("Análise das metas", goalHints()));
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
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma meta cadastrada."));
        table.getColumns().addAll(
                UiUtils.column("Nome", GoalDTO::name),
                UiUtils.column("Conta", GoalDTO::accountName),
                UiUtils.column("Valor alvo", item -> UiUtils.money(item.targetAmount())),
                UiUtils.column("Valor atual", item -> UiUtils.money(item.currentAmount())),
                UiUtils.column("Prazo", item -> UiUtils.date(item.deadline())),
                UiUtils.column("Progresso", item -> item.progressPercent() + "%"),
                UiUtils.column("Status", GoalDTO::statusLabel)
        );
        UiUtils.configureTable(table, 420);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> fill(value));
    }

    private void configureControls() {
        accountCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AccountDTO account) {
                return account == null ? "Sem conta vinculada" : account.name();
            }

            @Override
            public AccountDTO fromString(String string) {
                return null;
            }
        });
    }

    private VBox form() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Nome", nameField);
        UiUtils.addRow(grid, 1, "Valor alvo", targetField);
        UiUtils.addRow(grid, 2, "Valor atual", currentField);
        UiUtils.addRow(grid, 3, "Prazo", deadlinePicker);
        UiUtils.addRow(grid, 4, "Conta vinculada", accountCombo);

        Button save = UiUtils.primaryButton("Salvar");
        save.setOnAction(event -> save());
        Button reset = UiUtils.secondaryButton("Limpar formulário");
        reset.setOnAction(event -> clear());
        Button delete = UiUtils.dangerButton("Excluir");
        delete.setOnAction(event -> delete());
        return new VBox(10, grid, UiUtils.actions(save, reset), UiUtils.actions(delete));
    }

    private void save() {
        try {
            Long accountId = accountCombo.getValue() == null ? null : accountCombo.getValue().id();
            if (selected == null) {
                context.goalService().create(accountId, nameField.getText(),
                        MoneyFormatter.parseUserInput(targetField.getText()),
                        MoneyFormatter.parseUserInput(currentField.getText()),
                        deadlinePicker.getValue());
                Notification.success("Registro salvo com sucesso.");
            } else {
                context.goalService().update(selected.id(), accountId, nameField.getText(),
                        MoneyFormatter.parseUserInput(targetField.getText()),
                        MoneyFormatter.parseUserInput(currentField.getText()),
                        deadlinePicker.getValue());
                Notification.success("Registro atualizado com sucesso.");
            }
            refresh();
            clear();
        } catch (BusinessException | IllegalArgumentException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) {
            Notification.warning("Selecione uma meta.");
            return;
        }
        if (!Notification.confirm("Deseja realmente excluir este registro?")) {
            return;
        }
        context.goalService().delete(selected.id());
        Notification.success("Registro excluído com sucesso.");
        refresh();
        clear();
    }

    private void fill(GoalDTO goal) {
        selected = goal;
        if (goal == null) {
            return;
        }
        nameField.setText(goal.name());
        targetField.setText(goal.targetAmount().toPlainString().replace(".", ","));
        currentField.setText(goal.currentAmount().toPlainString().replace(".", ","));
        deadlinePicker.setValue(goal.deadline());
        if (goal.accountId() == null) {
            accountCombo.setValue(null);
        } else {
            accountCombo.getItems().stream()
                    .filter(account -> account.id().equals(goal.accountId()))
                    .findFirst()
                    .ifPresent(accountCombo::setValue);
        }
    }

    private void clear() {
        selected = null;
        table.getSelectionModel().clearSelection();
        nameField.clear();
        targetField.clear();
        currentField.setText("0,00");
        deadlinePicker.setValue(LocalDate.now().plusMonths(6));
        accountCombo.setValue(null);
    }

    private void refresh() {
        accountCombo.setItems(FXCollections.observableArrayList(context.accountService().listAccounts()));
        List<GoalDTO> goals = context.goalService().listGoals();
        table.setItems(FXCollections.observableArrayList(goals));
        UiUtils.adjustTableHeight(table, goals.size(), 300, 460);
        long completed = goals.stream().filter(goal -> goal.status() == GoalStatus.COMPLETED).count();
        long inProgress = goals.stream().filter(goal -> goal.status() == GoalStatus.IN_PROGRESS).count();
        long overdue = goals.stream().filter(goal -> goal.status() == GoalStatus.OVERDUE).count();
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Total de metas", String.valueOf(goals.size()), "card-accent"),
                UiUtils.summaryCard("Concluídas", String.valueOf(completed), "card-success"),
                UiUtils.summaryCard("Em andamento", String.valueOf(inProgress), "card-gold"),
                UiUtils.summaryCard("Atrasadas", String.valueOf(overdue), "card-danger")
        );
    }

    private VBox goalHints() {
        return new VBox(10,
                UiUtils.helperText("O progresso é calculado por valor atual dividido pelo valor alvo."),
                UiUtils.helperText("Metas concluídas aparecem quando o valor atual alcança ou supera o alvo."),
                UiUtils.helperText("A conta vinculada é opcional e serve apenas para organização da meta.")
        );
    }
}
