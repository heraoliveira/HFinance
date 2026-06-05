package com.hfinance.ui.component;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class Notification {
    private static StackPane host;

    private Notification() {
    }

    public enum RecurringEditChoice {
        CURRENT,
        CURRENT_AND_FUTURE,
        CANCEL
    }

    public static void install(StackPane notificationHost) {
        host = Objects.requireNonNull(notificationHost);
    }

    public static void success(String message) {
        toast("Sucesso", message, "toast-success");
    }

    public static void error(String message) {
        toast("Erro", message, "toast-error");
    }

    public static boolean confirm(String message) {
        return showModal("Confirmação", message, false, List.of(
                new ModalOption<>("Confirmar", true, "button-primary"),
                new ModalOption<>("Cancelar", false, "button-secondary")
        ));
    }

    public static RecurringEditChoice confirmRecurringEdit() {
        return showModal("Transação recorrente",
                "Escolha como esta edição deve ser aplicada.",
                RecurringEditChoice.CANCEL,
                List.of(
                        new ModalOption<>("Alterar somente esta transação", RecurringEditChoice.CURRENT, "button-primary"),
                        new ModalOption<>("Alterar esta e as próximas", RecurringEditChoice.CURRENT_AND_FUTURE, "button-secondary"),
                        new ModalOption<>("Cancelar", RecurringEditChoice.CANCEL, "button-secondary")
                ));
    }

    private static void toast(String title, String message, String styleClass) {
        Runnable show = () -> {
            if (host == null) {
                return;
            }
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("toast-title");
            Label messageLabel = new Label(safeMessage(message));
            messageLabel.getStyleClass().add("toast-message");
            messageLabel.setWrapText(true);

            VBox toast = new VBox(4, titleLabel, messageLabel);
            toast.getStyleClass().addAll("toast", styleClass);
            toast.setMaxWidth(380);
            StackPane.setAlignment(toast, Pos.TOP_RIGHT);
            StackPane.setMargin(toast, new Insets(18, 18, 0, 0));
            host.getChildren().add(toast);

            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(event -> host.getChildren().remove(toast));
            delay.play();
        };
        if (Platform.isFxApplicationThread()) {
            show.run();
        } else {
            Platform.runLater(show);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T showModal(String title, String message, T cancelValue, List<ModalOption<T>> options) {
        if (host == null || !Platform.isFxApplicationThread()) {
            return cancelValue;
        }

        Object loopKey = new Object();
        AtomicReference<T> selected = new AtomicReference<>(cancelValue);
        AtomicReference<StackPane> overlayRef = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");
        Label messageLabel = new Label(safeMessage(message));
        messageLabel.getStyleClass().add("modal-message");
        messageLabel.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        for (ModalOption<T> option : options) {
            Button button = new Button(option.label());
            button.getStyleClass().add(option.styleClass());
            button.setOnAction(event -> {
                selected.set(option.value());
                closeModal(loopKey, closed, selected.get(), overlayRef.get());
            });
            actions.getChildren().add(button);
        }

        VBox card = new VBox(14, titleLabel, messageLabel, actions);
        card.getStyleClass().add("modal-card");
        card.setMaxWidth(520);

        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add("modal-backdrop");
        overlay.setPickOnBounds(true);
        StackPane.setAlignment(card, Pos.CENTER);
        overlayRef.set(overlay);
        host.getChildren().add(overlay);

        return (T) Platform.enterNestedEventLoop(loopKey);
    }

    private static <T> void closeModal(Object loopKey, AtomicBoolean closed, T result, StackPane overlay) {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        if (host != null && overlay != null) {
            host.getChildren().remove(overlay);
        }
        Platform.exitNestedEventLoop(loopKey, result);
    }

    private static String safeMessage(String message) {
        return message == null || message.isBlank() ? "Não foi possível concluir a operação." : message;
    }

    private record ModalOption<T>(String label, T value, String styleClass) {
    }
}
