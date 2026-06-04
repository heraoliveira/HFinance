package com.hfinance.ui.controller;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.format.MoneyFormatter;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class AccountController {
    private final HFinanceContext context;
    private final TableView<AccountDTO> table = new TableView<>();
    private final TextField nameField = UiUtils.textField("Ex.: Conta principal");
    private final TextField institutionField = UiUtils.textField("Ex.: Banco");
    private final ComboBox<AccountType> typeCombo = new ComboBox<>();
    private final TextField initialBalanceField = UiUtils.textField("Ex.: 1.000,00");
    private final CheckBox activeCheck = new CheckBox("Conta ativa");
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private AccountDTO selected;

    public AccountController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        configureTable();
        configureFormControls();

        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        HBox layout = new HBox(16);
        VBox tablePanel = UiUtils.panel("Contas cadastradas", table);
        VBox sidePanel = new VBox(16, UiUtils.panel("Nova conta", form()), UiUtils.compactPanel("Análise das contas", accountHints()));
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        tablePanel.setMinWidth(620);
        sidePanel.setPrefWidth(390);
        layout.getChildren().addAll(tablePanel, sidePanel);
        root.getChildren().addAll(summaryCards, layout);
        refresh();
        return UiUtils.scrollable(root);
    }

    private void configureTable() {
        if (!table.getColumns().isEmpty()) {
            return;
        }
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma conta cadastrada."));
        table.getColumns().addAll(
                UiUtils.column("Nome", AccountDTO::name),
                UiUtils.column("Instituição", AccountDTO::institution),
                UiUtils.column("Tipo", AccountDTO::typeLabel),
                UiUtils.column("Saldo inicial", item -> UiUtils.money(item.initialBalance())),
                UiUtils.column("Saldo atual", item -> UiUtils.money(item.currentBalance())),
                UiUtils.column("Status", AccountDTO::statusLabel)
        );
        UiUtils.configureTable(table, 420);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> fillForm(value));
    }

    private void configureFormControls() {
        typeCombo.setItems(FXCollections.observableArrayList(Arrays.asList(AccountType.values())));
        typeCombo.getStyleClass().add("input");
        activeCheck.setSelected(true);
    }

    private VBox form() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Nome", nameField);
        UiUtils.addRow(grid, 1, "Instituição", institutionField);
        UiUtils.addRow(grid, 2, "Tipo", typeCombo);
        UiUtils.addRow(grid, 3, "Saldo inicial", initialBalanceField);
        UiUtils.addRow(grid, 4, "Status", activeCheck);

        Button save = UiUtils.primaryButton("Salvar");
        save.setOnAction(event -> save());
        Button newAccount = UiUtils.secondaryButton("Nova conta");
        newAccount.setOnAction(event -> newSimilar());
        Button reset = UiUtils.secondaryButton("Limpar formulário");
        reset.setOnAction(event -> clear());
        Button deactivate = UiUtils.secondaryButton("Inativar");
        deactivate.setOnAction(event -> deactivate());
        Button delete = UiUtils.dangerButton("Excluir");
        delete.setOnAction(event -> delete());

        return new VBox(10, grid, UiUtils.actions(save, newAccount, reset), UiUtils.actions(deactivate, delete));
    }

    private void save() {
        try {
            if (selected == null) {
                context.accountService().create(nameField.getText(), institutionField.getText(),
                        typeCombo.getValue(), MoneyFormatter.parseUserInput(initialBalanceField.getText()));
                Notification.success("Registro salvo com sucesso.");
            } else {
                context.accountService().update(selected.id(), nameField.getText(), institutionField.getText(),
                        typeCombo.getValue(), MoneyFormatter.parseUserInput(initialBalanceField.getText()),
                        activeCheck.isSelected());
                Notification.success("Registro atualizado com sucesso.");
            }
            refresh();
            newSimilar();
        } catch (BusinessException | IllegalArgumentException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void deactivate() {
        if (selected == null) {
            Notification.error("Selecione uma conta.");
            return;
        }
        context.accountService().deactivate(selected.id());
        Notification.success("Registro atualizado com sucesso.");
        clear();
        refresh();
    }

    private void delete() {
        if (selected == null) {
            Notification.error("Selecione uma conta.");
            return;
        }
        if (Notification.confirm("Deseja realmente excluir este registro?").orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            context.accountService().delete(selected.id());
            Notification.success("Registro excluído com sucesso.");
            clear();
            refresh();
        } catch (BusinessException ex) {
            Notification.error(ex.getMessage());
        }
    }

    private void fillForm(AccountDTO account) {
        selected = account;
        if (account == null) {
            return;
        }
        nameField.setText(account.name());
        institutionField.setText(account.institution());
        typeCombo.setValue(account.type());
        initialBalanceField.setText(account.initialBalance().toPlainString().replace(".", ","));
        activeCheck.setSelected(account.active());
    }

    private void clear() {
        selected = null;
        table.getSelectionModel().clearSelection();
        nameField.clear();
        institutionField.clear();
        typeCombo.setValue(AccountType.CHECKING_ACCOUNT);
        initialBalanceField.setText("0,00");
        activeCheck.setSelected(true);
    }

    private void newSimilar() {
        selected = null;
        table.getSelectionModel().clearSelection();
        nameField.clear();
        if (typeCombo.getValue() == null) {
            typeCombo.setValue(AccountType.CHECKING_ACCOUNT);
        }
        if (initialBalanceField.getText() == null || initialBalanceField.getText().isBlank()) {
            initialBalanceField.setText("0,00");
        }
        nameField.requestFocus();
    }

    private void refresh() {
        List<AccountDTO> accounts = context.accountService().listAccounts();
        table.setItems(FXCollections.observableArrayList(accounts));
        UiUtils.adjustTableHeight(table, accounts.size(), 300, 460);
        BigDecimal totalBalance = accounts.stream()
                .filter(AccountDTO::active)
                .map(AccountDTO::currentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeAccounts = accounts.stream().filter(AccountDTO::active).count();
        long inactiveAccounts = accounts.size() - activeAccounts;
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Total de contas", String.valueOf(accounts.size()), "card-accent"),
                UiUtils.summaryCard("Contas ativas", String.valueOf(activeAccounts), "card-success"),
                UiUtils.summaryCard("Saldo total", UiUtils.money(totalBalance), "card-gold"),
                UiUtils.summaryCard("Contas inativas", String.valueOf(inactiveAccounts), "card-danger")
        );
    }

    private VBox accountHints() {
        return new VBox(10,
                UiUtils.helperText("O saldo atual é calculado automaticamente por saldo inicial + receitas - despesas."),
                UiUtils.helperText("Contas com transações devem ser inativadas em vez de excluídas fisicamente."),
                UiUtils.helperText("Selecione uma linha da tabela para editar, inativar ou excluir quando permitido.")
        );
    }
}
