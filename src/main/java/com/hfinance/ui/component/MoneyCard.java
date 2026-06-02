package com.hfinance.ui.component;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MoneyCard extends VBox {
    private final Label valueLabel = new Label();

    public MoneyCard(String title, String value, String styleClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        valueLabel.setText(value);
        valueLabel.getStyleClass().add("card-value");
        valueLabel.setWrapText(true);

        getStyleClass().addAll("card", styleClass);
        setMinWidth(190);
        setPrefWidth(220);
        setPadding(new Insets(18));
        setSpacing(8);
        getChildren().addAll(titleLabel, valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
