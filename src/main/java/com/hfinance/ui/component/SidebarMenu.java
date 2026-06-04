package com.hfinance.ui.component;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarMenu extends VBox {
    private final List<Button> buttons = new ArrayList<>();

    public SidebarMenu(Consumer<String> onNavigate) {
        getStyleClass().add("sidebar");
        setPadding(new Insets(22, 16, 22, 16));
        setSpacing(10);

        Label brand = new Label("HFinance");
        brand.getStyleClass().add("sidebar-brand");
        Label subtitle = new Label("Finanças pessoais");
        subtitle.getStyleClass().add("sidebar-subtitle");
        getChildren().addAll(brand, subtitle);

        addButton("Visão geral", onNavigate);
        addButton("Contas", onNavigate);
        addButton("Transações", onNavigate);
        addButton("Categorias", onNavigate);
        addButton("Orçamentos", onNavigate);
        addButton("Metas", onNavigate);
        addButton("Relatórios", onNavigate);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        addButton("Sobre", onNavigate);
    }

    private void addButton(String label, Consumer<String> onNavigate) {
        Button button = new Button(label);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            setActive(button);
            onNavigate.accept(label);
        });
        buttons.add(button);
        getChildren().add(button);
    }

    public void activate(String label) {
        buttons.stream()
                .filter(button -> button.getText().equals(label))
                .findFirst()
                .ifPresent(this::setActive);
    }

    private void setActive(Button activeButton) {
        buttons.forEach(button -> button.getStyleClass().remove("sidebar-button-active"));
        activeButton.getStyleClass().add("sidebar-button-active");
    }
}
