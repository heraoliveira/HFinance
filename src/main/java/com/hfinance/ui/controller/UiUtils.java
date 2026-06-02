package com.hfinance.ui.controller;

import com.hfinance.core.format.DateFormatter;
import com.hfinance.core.format.MoneyFormatter;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

final class UiUtils {
    private UiUtils() {
    }

    static TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input");
        return field;
    }

    static Button primaryButton(String label) {
        Button button = new Button(label);
        button.getStyleClass().add("button-primary");
        return button;
    }

    static Button secondaryButton(String label) {
        Button button = new Button(label);
        button.getStyleClass().add("button-secondary");
        return button;
    }

    static Button dangerButton(String label) {
        Button button = new Button(label);
        button.getStyleClass().add("button-danger");
        return button;
    }

    static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(14, 0, 0, 0));
        return grid;
    }

    static void addRow(GridPane grid, int row, String label, Node node) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");
        grid.add(labelNode, 0, row);
        grid.add(node, 1, row);
        GridPane.setHgrow(node, Priority.ALWAYS);
    }

    static VBox panel(String title, Node content) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        label.setWrapText(true);
        VBox panel = new VBox(12, label, content);
        panel.getStyleClass().add("panel");
        panel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    static VBox compactPanel(String title, Node content) {
        VBox panel = panel(title, content);
        panel.getStyleClass().add("panel-compact");
        VBox.setVgrow(content, Priority.NEVER);
        return panel;
    }

    static ScrollPane scrollable(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("content-scroll");
        return scrollPane;
    }

    static FlowPane summaryCards(Node... cards) {
        FlowPane flowPane = new FlowPane(16, 16);
        flowPane.getStyleClass().add("summary-grid");
        flowPane.getChildren().addAll(cards);
        return flowPane;
    }

    static VBox summaryCard(String title, String value, String styleClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");
        valueLabel.setWrapText(true);

        VBox card = new VBox(8, titleLabel, valueLabel);
        card.getStyleClass().addAll("card", "summary-card");
        if (styleClass != null && !styleClass.isBlank()) {
            card.getStyleClass().add(styleClass);
        }
        card.setMinWidth(190);
        card.setPrefWidth(220);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    static Label helperText(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("helper-text");
        label.setWrapText(true);
        return label;
    }

    static void configureTable(TableView<?> table, double prefHeight) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        table.setMinHeight(Math.min(prefHeight, 260));
        table.setPrefHeight(prefHeight);
        table.setMaxHeight(prefHeight + 140);
    }

    static void adjustTableHeight(TableView<?> table, int rowCount, double emptyHeight, double maxHeight) {
        double calculatedHeight = rowCount == 0
                ? emptyHeight
                : Math.min(maxHeight, Math.max(emptyHeight, 130 + (rowCount * 34.0)));
        table.setMinHeight(Math.min(calculatedHeight, 260));
        table.setPrefHeight(calculatedHeight);
        table.setMaxHeight(calculatedHeight);
    }

    static HBox actions(Node... nodes) {
        HBox box = new HBox(10, nodes);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    static <T> TableColumn<T, String> column(String title, Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(mapper.apply(data.getValue())));
        return column;
    }

    static String money(BigDecimal value) {
        return MoneyFormatter.format(value);
    }

    static String date(LocalDate date) {
        return DateFormatter.format(date);
    }
}
