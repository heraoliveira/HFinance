package com.hfinance.ui.controller;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.DashboardSummaryDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.ui.component.MoneyCard;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardController {
    private final HFinanceContext context;

    public DashboardController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        DashboardSummaryDTO summary = context.dashboardService().summary();
        VBox root = new VBox(18);
        root.setPadding(new Insets(0));
        root.setMaxWidth(Double.MAX_VALUE);

        FlowPane cards = UiUtils.summaryCards();
        cards.getChildren().addAll(
                new MoneyCard("Saldo total", UiUtils.money(summary.totalBalance()), "card-accent"),
                new MoneyCard("Receitas do mês", UiUtils.money(summary.currentMonthIncome()), "card-success"),
                new MoneyCard("Despesas do mês", UiUtils.money(summary.currentMonthExpense()), "card-danger"),
                new MoneyCard("Saldo do mês", UiUtils.money(summary.currentMonthBalance()), "card-gold")
        );

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);
        rightColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(leftColumn, rightColumn);

        VBox chartPanel = UiUtils.compactPanel("Evolução mensal", monthlyEvolution(summary));
        VBox latestPanel = UiUtils.panel("Últimas transações", latestTable(summary));
        VBox accountPanel = UiUtils.panel("Resumo por conta", accountTable(summary));
        VBox budgetPanel = UiUtils.panel("Alertas de orçamento", budgetTable(summary));
        VBox goalPanel = UiUtils.panel("Progresso das metas", goalTable(summary));
        grid.add(chartPanel, 0, 0, 2, 1);
        grid.add(latestPanel, 0, 1);
        grid.add(accountPanel, 1, 1);
        grid.add(budgetPanel, 0, 2);
        grid.add(goalPanel, 1, 2);
        GridPane.setHgrow(chartPanel, Priority.ALWAYS);
        GridPane.setHgrow(latestPanel, Priority.ALWAYS);
        GridPane.setHgrow(accountPanel, Priority.ALWAYS);
        GridPane.setHgrow(budgetPanel, Priority.ALWAYS);
        GridPane.setHgrow(goalPanel, Priority.ALWAYS);

        root.getChildren().addAll(cards, grid);
        return UiUtils.scrollable(root);
    }

    private Region monthlyEvolution(DashboardSummaryDTO summary) {
        boolean hasData = summary.monthlyIncome().values().stream().anyMatch(value -> value.signum() > 0)
                || summary.monthlyExpense().values().stream().anyMatch(value -> value.signum() > 0);
        if (!hasData) {
            VBox emptyState = new VBox(8,
                    UiUtils.helperText("Nenhum dado encontrado para a evolução mensal."),
                    UiUtils.helperText("Cadastre receitas ou despesas para visualizar o gráfico comparativo por mês.")
            );
            emptyState.setMinHeight(110);
            emptyState.setPrefHeight(120);
            emptyState.setMaxHeight(140);
            return emptyState;
        }
        return monthlyChart(summary);
    }

    private BarChart<String, Number> monthlyChart(DashboardSummaryDTO summary) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setAnimated(false);
        chart.setTitle("Receitas e despesas por mês");
        chart.setMinHeight(230);
        chart.setPrefHeight(250);
        chart.setMaxHeight(270);

        XYChart.Series<String, Number> income = new XYChart.Series<>();
        income.setName("Receitas");
        summary.monthlyIncome().forEach((month, value) -> income.getData().add(new XYChart.Data<>(month, value)));

        XYChart.Series<String, Number> expense = new XYChart.Series<>();
        expense.setName("Despesas");
        summary.monthlyExpense().forEach((month, value) -> expense.getData().add(new XYChart.Data<>(month, value)));

        chart.getData().addAll(income, expense);
        return chart;
    }

    private TableView<TransactionDTO> latestTable(DashboardSummaryDTO summary) {
        TableView<TransactionDTO> table = new TableView<>(FXCollections.observableArrayList(summary.latestTransactions()));
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma transação cadastrada."));
        table.getColumns().addAll(
                UiUtils.column("Data", item -> UiUtils.date(item.transactionDate())),
                UiUtils.column("Tipo", TransactionDTO::transactionTypeLabel),
                UiUtils.column("Descrição", TransactionDTO::description),
                UiUtils.column("Valor", item -> UiUtils.money(item.amount()))
        );
        UiUtils.configureTable(table, 220);
        return table;
    }

    private TableView<AccountDTO> accountTable(DashboardSummaryDTO summary) {
        TableView<AccountDTO> table = new TableView<>(FXCollections.observableArrayList(summary.accountBalances()));
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma conta cadastrada."));
        table.getColumns().addAll(
                UiUtils.column("Conta", AccountDTO::name),
                UiUtils.column("Tipo", AccountDTO::typeLabel),
                UiUtils.column("Saldo", item -> UiUtils.money(item.currentBalance())),
                UiUtils.column("Status", AccountDTO::statusLabel)
        );
        UiUtils.configureTable(table, 220);
        return table;
    }

    private TableView<BudgetDTO> budgetTable(DashboardSummaryDTO summary) {
        TableView<BudgetDTO> table = new TableView<>(FXCollections.observableArrayList(summary.exceededBudgets()));
        table.setPlaceholder(new javafx.scene.control.Label("Nenhum orçamento excedido."));
        table.getColumns().addAll(
                UiUtils.column("Orçamento", BudgetDTO::name),
                UiUtils.column("Categoria", BudgetDTO::categoryName),
                UiUtils.column("Gasto", item -> UiUtils.money(item.spentAmount())),
                UiUtils.column("Status", BudgetDTO::statusLabel)
        );
        UiUtils.configureTable(table, 200);
        return table;
    }

    private TableView<GoalDTO> goalTable(DashboardSummaryDTO summary) {
        TableView<GoalDTO> table = new TableView<>(FXCollections.observableArrayList(summary.goals()));
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma meta cadastrada."));
        table.getColumns().addAll(
                UiUtils.column("Meta", GoalDTO::name),
                UiUtils.column("Prazo", item -> UiUtils.date(item.deadline())),
                UiUtils.column("Progresso", item -> item.progressPercent() + "%"),
                UiUtils.column("Status", GoalDTO::statusLabel)
        );
        UiUtils.configureTable(table, 200);
        return table;
    }
}
