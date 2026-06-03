package com.hfinance.core.error;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class ErrorDialogService {
    private ErrorDialogService() {
    }

    public static void showUnexpectedError(Path logPath) {
        Runnable dialog = () -> {
            ButtonType openLogs = new ButtonType("Abrir logs");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("""
                    Ocorreu um erro inesperado.

                    Se o problema continuar, envie o arquivo de log para a desenvolvedora.

                    Local do log:
                    %s
                    """.formatted(logPath.toAbsolutePath()));
            alert.getButtonTypes().setAll(openLogs, ButtonType.OK);
            Optional<ButtonType> choice = alert.showAndWait();
            if (choice.isPresent() && choice.get() == openLogs) {
                openDirectory(logPath.getParent());
            }
        };
        if (Platform.isFxApplicationThread()) {
            dialog.run();
        } else {
            Platform.runLater(dialog);
        }
    }

    public static void showStartupError(Path dataPath, Path backupsPath, Path logPath) {
        ButtonType openBackups = new ButtonType("Abrir backups");
        ButtonType openData = new ButtonType("Abrir dados");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro ao iniciar");
        alert.setHeaderText(null);
        alert.setContentText("""
                Não foi possível abrir o banco de dados do HFinance com segurança.

                Se o problema continuar, abra a pasta de backups e restaure uma cópia anterior.

                Local dos backups:
                %s

                Local do log:
                %s
                """.formatted(backupsPath.toAbsolutePath(), logPath.toAbsolutePath()));
        alert.getButtonTypes().setAll(openBackups, openData, ButtonType.OK);
        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == openBackups) {
            openDirectory(backupsPath);
        } else if (choice.isPresent() && choice.get() == openData) {
            openDirectory(dataPath);
        }
    }

    private static void openDirectory(Path directory) {
        try {
            if (directory != null && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory.toFile());
            }
        } catch (IOException | RuntimeException ignored) {
            // O alerta principal já informa o caminho para abertura manual.
        }
    }
}
