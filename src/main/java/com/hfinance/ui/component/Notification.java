package com.hfinance.ui.component;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class Notification {
    private Notification() {
    }

    public static void success(String message) {
        show(Alert.AlertType.INFORMATION, "Sucesso", message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Erro", message);
    }

    public static Optional<ButtonType> confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
