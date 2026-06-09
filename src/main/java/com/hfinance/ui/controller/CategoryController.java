package com.hfinance.ui.controller;

import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.ui.component.Notification;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;

public class CategoryController {
    private static final String DEFAULT_COLOR = "#00E5FF";

    private final HFinanceContext context;
    private final TableView<CategoryDTO> table = new TableView<>();
    private final TextField nameField = UiUtils.textField("Ex.: Mercado");
    private final ComboBox<CategoryType> typeCombo = new ComboBox<>();
    private final ColorPicker colorPicker = new ColorPicker(Color.web(DEFAULT_COLOR));
    private final CheckBox activeCheck = new CheckBox("Categoria ativa");
    private final ComboBox<CategoryType> typeFilter = new ComboBox<>();
    private final FlowPane summaryCards = UiUtils.summaryCards();
    private final Button statusButton = UiUtils.secondaryButton("Inativar");
    private CategoryDTO selected;

    public CategoryController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        configureTable();
        configureControls();

        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        HBox layout = new HBox(16);
        VBox tablePanel = UiUtils.panel("Categorias cadastradas", table);
        VBox sidePanel = new VBox(16,
                UiUtils.panel("Categoria", form()),
                UiUtils.compactPanel("Gestão de categorias", categoryHints()));
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        tablePanel.setMinWidth(640);
        sidePanel.setPrefWidth(390);
        layout.getChildren().addAll(tablePanel, sidePanel);
        root.getChildren().addAll(summaryCards, UiUtils.compactPanel("Filtros", filters()), layout);
        refresh();
        clear();
        return UiUtils.scrollable(root);
    }

    private void configureTable() {
        if (!table.getColumns().isEmpty()) {
            return;
        }
        table.setPlaceholder(new javafx.scene.control.Label("Nenhuma categoria cadastrada."));
        table.getColumns().addAll(
                nameColumn(),
                UiUtils.column("Tipo", CategoryDTO::typeLabel),
                UiUtils.column("Origem", CategoryDTO::originLabel),
                UiUtils.column("Status", CategoryDTO::statusLabel)
        );
        UiUtils.configureTable(table, 420);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> fill(value));
    }

    private void configureControls() {
        StringConverter<CategoryType> converter = categoryTypeConverter();
        typeCombo.setConverter(converter);
        typeFilter.setConverter(converter);
        typeCombo.setItems(FXCollections.observableArrayList(Arrays.asList(CategoryType.values())));
        typeFilter.setItems(FXCollections.observableArrayList(Arrays.asList(CategoryType.values())));
        typeCombo.getStyleClass().add("input");
        typeFilter.getStyleClass().add("input");
        colorPicker.getStyleClass().add("input");
        activeCheck.setSelected(true);
        activeCheck.setDisable(true);
    }

    private VBox filters() {
        Button apply = UiUtils.primaryButton("Aplicar filtros");
        apply.setOnAction(event -> refresh());
        Button clear = UiUtils.secondaryButton("Limpar filtros");
        clear.setOnAction(event -> {
            typeFilter.setValue(null);
            refresh();
        });
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Tipo", typeFilter);
        grid.add(UiUtils.actions(apply, clear), 1, 1);
        return new VBox(10, grid);
    }

    private VBox form() {
        GridPane grid = UiUtils.formGrid();
        UiUtils.addRow(grid, 0, "Nome", nameField);
        UiUtils.addRow(grid, 1, "Tipo", typeCombo);
        UiUtils.addRow(grid, 2, "Cor", colorPicker);
        UiUtils.addRow(grid, 3, "Status", activeCheck);

        Button save = UiUtils.primaryButton("Salvar");
        save.setOnAction(event -> save());
        Button reset = UiUtils.secondaryButton("Limpar formulário");
        reset.setOnAction(event -> clear());
        statusButton.setOnAction(event -> toggleStatus());
        Button delete = UiUtils.dangerButton("Excluir");
        delete.setOnAction(event -> delete());
        return new VBox(10, grid, UiUtils.actions(save, reset), UiUtils.actions(statusButton, delete));
    }

    private void save() {
        try {
            if (selected == null) {
                context.categoryService().create(nameField.getText(), typeCombo.getValue(), selectedColor());
                Notification.success("Categoria salva com sucesso.");
            } else {
                context.categoryService().update(selected.id(), nameField.getText(), typeCombo.getValue(),
                        selectedColor(), activeCheck.isSelected());
                Notification.success("Categoria atualizada com sucesso.");
            }
            refresh();
            clear();
        } catch (BusinessException | IllegalArgumentException ex) {
            Notification.error(ex.getMessage() == null ? "Não foi possível concluir a operação." : ex.getMessage());
        }
    }

    private void toggleStatus() {
        if (selected == null) {
            Notification.warning("Selecione uma categoria.");
            return;
        }
        try {
            if (selected.active()) {
                if (!Notification.confirm("Deseja realmente inativar esta categoria?")) {
                    return;
                }
                context.categoryService().deactivate(selected.id());
                Notification.success("Categoria inativada com sucesso.");
            } else {
                context.categoryService().reactivate(selected.id());
                Notification.success("Categoria reativada com sucesso.");
            }
            refresh();
            clear();
        } catch (BusinessException ex) {
            Notification.error(ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) {
            Notification.warning("Selecione uma categoria.");
            return;
        }
        if (!Notification.confirm("Deseja realmente excluir esta categoria?")) {
            return;
        }
        try {
            context.categoryService().delete(selected.id());
            Notification.success("Categoria excluída com sucesso.");
            refresh();
            clear();
        } catch (BusinessException ex) {
            Notification.error(ex.getMessage());
        }
    }

    private void fill(CategoryDTO category) {
        selected = category;
        if (category == null) {
            return;
        }
        nameField.setText(category.name());
        typeCombo.setValue(category.type());
        colorPicker.setValue(color(category.color()));
        activeCheck.setSelected(category.active());
        activeCheck.setDisable(false);
        statusButton.setText(category.active() ? "Inativar" : "Reativar");
    }

    private void clear() {
        selected = null;
        table.getSelectionModel().clearSelection();
        nameField.clear();
        typeCombo.setValue(CategoryType.EXPENSE);
        colorPicker.setValue(Color.web(DEFAULT_COLOR));
        activeCheck.setSelected(true);
        activeCheck.setDisable(true);
        statusButton.setText("Inativar");
    }

    private void refresh() {
        List<CategoryDTO> categories = typeFilter.getValue() == null
                ? context.categoryService().listAll()
                : context.categoryService().listByType(typeFilter.getValue());
        table.setItems(FXCollections.observableArrayList(categories));
        UiUtils.adjustTableHeight(table, categories.size(), 300, 460);
        long active = categories.stream().filter(CategoryDTO::active).count();
        long inactive = categories.size() - active;
        long custom = categories.stream().filter(category -> !category.defaultCategory()).count();
        summaryCards.getChildren().setAll(
                UiUtils.summaryCard("Total de categorias", String.valueOf(categories.size()), "card-accent"),
                UiUtils.summaryCard("Categorias ativas", String.valueOf(active), "card-success"),
                UiUtils.summaryCard("Personalizadas", String.valueOf(custom), "card-gold"),
                UiUtils.summaryCard("Inativas", String.valueOf(inactive), "card-danger")
        );
    }

    private VBox categoryHints() {
        return new VBox(10,
                UiUtils.helperText("Categorias inativas não aparecem como opção padrão em novos cadastros."),
                UiUtils.helperText("Categorias sem uso podem ser excluídas, inclusive as padrão."),
                UiUtils.helperText("Categorias usadas em transações ou orçamentos devem ser inativadas em vez de excluídas."),
                UiUtils.helperText("O nome ativo é único por tipo, ignorando maiúsculas, minúsculas e espaços extras.")
        );
    }

    private TableColumn<CategoryDTO, String> nameColumn() {
        TableColumn<CategoryDTO, String> column = UiUtils.column("Nome", CategoryDTO::name);
        column.setCellFactory(tableColumn -> new TableCell<>() {
            private final Region swatch = new Region();
            private final Label label = new Label();
            private final HBox box = new HBox(8, swatch, label);

            {
                swatch.getStyleClass().add("category-swatch");
                swatch.setMinSize(12, 12);
                swatch.setPrefSize(12, 12);
            }

            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                CategoryDTO category = getTableView().getItems().get(getIndex());
                swatch.setStyle("-fx-background-color: %s;".formatted(category.color()));
                label.setText(name);
                setGraphic(box);
                setText(null);
            }
        });
        return column;
    }

    private String selectedColor() {
        Color color = colorPicker.getValue() == null ? Color.web(DEFAULT_COLOR) : colorPicker.getValue();
        return "#%02X%02X%02X".formatted(
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

    private Color color(String value) {
        try {
            return Color.web(value == null || value.isBlank() ? DEFAULT_COLOR : value);
        } catch (IllegalArgumentException ex) {
            return Color.web(DEFAULT_COLOR);
        }
    }

    private StringConverter<CategoryType> categoryTypeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(CategoryType type) {
                return type == null ? "Todas" : type.displayName();
            }

            @Override
            public CategoryType fromString(String string) {
                return null;
            }
        };
    }
}
