package com.hfinance.ui.navigation;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Router {
    private final BorderPane root;
    private final Label title;
    private final Map<String, Supplier<Node>> routes = new LinkedHashMap<>();

    public Router(BorderPane root, Label title) {
        this.root = root;
        this.title = title;
    }

    public void register(String label, Supplier<Node> viewSupplier) {
        routes.put(label, viewSupplier);
    }

    public void navigate(String label) {
        Supplier<Node> supplier = routes.get(label);
        if (supplier != null) {
            title.setText(label);
            root.setCenter(supplier.get());
        }
    }
}
