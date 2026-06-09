package com.hfinance.ui.component;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class Notification {
    private static final double TOAST_PREFERRED_WIDTH = 360;
    private static final double TOAST_MAX_WIDTH = 400;
    private static final double MODAL_PREFERRED_WIDTH = 440;
    private static final double MODAL_MAX_WIDTH = 520;
    private static final int LONG_MESSAGE_THRESHOLD = 180;

    private static StackPane host;
    private static VBox toastStack;

    private Notification() {
    }

    public enum RecurringEditChoice {
        CURRENT,
        CURRENT_AND_FUTURE,
        CANCEL
    }

    private enum NotificationType {
        SUCCESS("Sucesso", "toast-success", Duration.seconds(4)),
        ERROR("Erro", "toast-error", Duration.seconds(6)),
        WARNING("Atenção", "toast-warning", Duration.seconds(6)),
        INFO("Informação", "toast-info", Duration.seconds(4));

        private final String title;
        private final String styleClass;
        private final Duration duration;

        NotificationType(String title, String styleClass, Duration duration) {
            this.title = title;
            this.styleClass = styleClass;
            this.duration = duration;
        }
    }

    public static void install(StackPane notificationHost) {
        host = Objects.requireNonNull(notificationHost);
        toastStack = new VBox(10);
        toastStack.getStyleClass().add("toast-stack");
        toastStack.setAlignment(Pos.TOP_RIGHT);
        toastStack.setPickOnBounds(false);
        toastStack.setPrefWidth(TOAST_PREFERRED_WIDTH);
        toastStack.setMaxWidth(TOAST_MAX_WIDTH);
        toastStack.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(toastStack, Pos.TOP_RIGHT);
        StackPane.setMargin(toastStack, new Insets(18));
        host.getChildren().add(toastStack);
    }

    public static boolean isInstalled() {
        return host != null && toastStack != null;
    }

    public static void success(String message) {
        toast(NotificationType.SUCCESS, message);
    }

    public static void error(String message) {
        toast(NotificationType.ERROR, message);
    }

    public static void warning(String message) {
        toast(NotificationType.WARNING, message);
    }

    public static void info(String message) {
        toast(NotificationType.INFO, message);
    }

    public static boolean confirm(String message) {
        return showModal("Confirmação", message, false, List.of(
                new ModalOption<>("Confirmar", true, "button-primary", true, false),
                new ModalOption<>("Cancelar", false, "button-secondary", false, true)
        ));
    }

    public static RecurringEditChoice confirmRecurringEdit() {
        return showModal("Transação recorrente",
                "Escolha como esta edição deve ser aplicada.",
                RecurringEditChoice.CANCEL,
                List.of(
                        new ModalOption<>("Alterar somente esta transação", RecurringEditChoice.CURRENT,
                                "button-primary", true, false),
                        new ModalOption<>("Alterar esta e as próximas", RecurringEditChoice.CURRENT_AND_FUTURE,
                                "button-secondary", false, false),
                        new ModalOption<>("Cancelar", RecurringEditChoice.CANCEL,
                                "button-secondary", false, true)
                ));
    }

    private static void toast(NotificationType type, String message) {
        Runnable show = () -> {
            if (!isInstalled()) {
                return;
            }

            Label titleLabel = new Label(type.title);
            titleLabel.getStyleClass().add("toast-title");

            Button closeButton = new Button("×");
            closeButton.getStyleClass().add("toast-close");
            closeButton.setFocusTraversable(false);
            closeButton.setAccessibleText("Fechar notificação");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox header = new HBox(8, titleLabel, spacer, closeButton);
            header.setAlignment(Pos.CENTER_LEFT);

            Node messageNode = messageNode(safeMessage(message));
            VBox toast = new VBox(8, header, messageNode);
            toast.getStyleClass().addAll("toast", type.styleClass);
            toast.setMinWidth(Region.USE_PREF_SIZE);
            toast.setPrefWidth(TOAST_PREFERRED_WIDTH);
            toast.setMaxHeight(Region.USE_PREF_SIZE);
            toast.maxWidthProperty().bind(Bindings.min(
                    TOAST_MAX_WIDTH,
                    host.widthProperty().subtract(36)));

            PauseTransition delay = new PauseTransition(type.duration);
            Runnable close = () -> {
                delay.stop();
                toast.maxWidthProperty().unbind();
                toastStack.getChildren().remove(toast);
            };
            closeButton.setOnAction(event -> close.run());
            toastStack.getChildren().add(toast);
            delay.setOnFinished(event -> close.run());
            delay.play();
        };
        if (Platform.isFxApplicationThread()) {
            show.run();
        } else {
            Platform.runLater(show);
        }
    }

    private static Node messageNode(String message) {
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("toast-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        if (message.length() <= LONG_MESSAGE_THRESHOLD && message.lines().count() <= 4) {
            return messageLabel;
        }

        ScrollPane scrollPane = new ScrollPane(messageLabel);
        scrollPane.getStyleClass().add("toast-message-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(96);
        scrollPane.setMaxHeight(120);
        return scrollPane;
    }

    @SuppressWarnings("unchecked")
    private static <T> T showModal(String title, String message, T cancelValue, List<ModalOption<T>> options) {
        if (!isInstalled() || !Platform.isFxApplicationThread()) {
            return cancelValue;
        }

        Object loopKey = new Object();
        AtomicReference<T> selected = new AtomicReference<>(cancelValue);
        AtomicReference<StackPane> overlayRef = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<Button> primaryButton = new AtomicReference<>();
        AtomicReference<Button> cancelButton = new AtomicReference<>();

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");
        Label messageLabel = new Label(safeMessage(message));
        messageLabel.getStyleClass().add("modal-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        FlowPane actions = new FlowPane(Orientation.HORIZONTAL, 10, 10);
        actions.getStyleClass().add("modal-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setRowValignment(javafx.geometry.VPos.CENTER);

        for (ModalOption<T> option : options) {
            Button button = new Button(option.label());
            button.getStyleClass().add(option.styleClass());
            button.setDefaultButton(option.primary());
            button.setCancelButton(option.cancel());
            button.setOnAction(event -> {
                selected.set(option.value());
                closeModal(loopKey, closed, selected.get(), overlayRef.get());
            });
            if (option.primary()) {
                primaryButton.set(button);
            }
            if (option.cancel()) {
                cancelButton.set(button);
            }
            actions.getChildren().add(button);
        }

        VBox card = new VBox(14, titleLabel, messageLabel, actions);
        card.getStyleClass().add("modal-card");
        card.setMinWidth(Region.USE_PREF_SIZE);
        card.setPrefWidth(MODAL_PREFERRED_WIDTH);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.maxWidthProperty().bind(Bindings.min(
                MODAL_MAX_WIDTH,
                host.widthProperty().subtract(64)));

        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add("modal-backdrop");
        overlay.setPickOnBounds(true);
        StackPane.setAlignment(card, Pos.CENTER);
        overlayRef.set(overlay);
        host.getChildren().add(overlay);

        overlay.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && cancelButton.get() != null) {
                cancelButton.get().fire();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER && primaryButton.get() != null) {
                primaryButton.get().fire();
                event.consume();
            }
        });
        Platform.runLater(() -> {
            overlay.requestFocus();
            if (primaryButton.get() != null) {
                primaryButton.get().requestFocus();
            }
        });

        return (T) Platform.enterNestedEventLoop(loopKey);
    }

    private static <T> void closeModal(Object loopKey, AtomicBoolean closed, T result, StackPane overlay) {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        if (host != null && overlay != null) {
            Node card = overlay.getChildren().isEmpty() ? null : overlay.getChildren().get(0);
            if (card instanceof Region region) {
                region.maxWidthProperty().unbind();
            }
            host.getChildren().remove(overlay);
        }
        Platform.exitNestedEventLoop(loopKey, result);
    }

    private static String safeMessage(String message) {
        return message == null || message.isBlank() ? "Não foi possível concluir a operação." : message;
    }

    private record ModalOption<T>(
            String label,
            T value,
            String styleClass,
            boolean primary,
            boolean cancel
    ) {
    }
}
