package com.hfinance.ui.controller;

import com.hfinance.core.backup.BackupResult;
import com.hfinance.core.backup.BackupType;
import com.hfinance.core.config.AppPaths;
import com.hfinance.core.config.AppVersion;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.database.DatabaseHealthResult;
import com.hfinance.core.logging.AppLogger;
import com.hfinance.ui.component.Notification;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger LOGGER = AppLogger.logger(AboutController.class);

    private final HFinanceContext context;
    private final Label lastBackupLabel = UiUtils.helperText("");
    private final Label schemaVersionLabel = UiUtils.helperText("");
    private final Label integrityLabel = UiUtils.helperText("");

    public AboutController(HFinanceContext context) {
        this.context = context;
    }

    public Parent getView() {
        refreshStatus();

        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().addAll(
                UiUtils.summaryCards(
                        UiUtils.summaryCard("Versão", AppVersion.VERSION, "card-accent"),
                        UiUtils.summaryCard("Banco", databaseExistsLabel(), "card-gold"),
                        UiUtils.summaryCard("Backups", String.valueOf(context.backupService().backupCount()), "card-success"),
                        UiUtils.summaryCard("Logs", Files.exists(context.appPaths().logsDirectory()) ? "Ativos" : "Pendente", "card-accent")
                ),
                UiUtils.panel("Aplicação", applicationInfo()),
                UiUtils.panel("Segurança dos dados", dataSafety()),
                UiUtils.panel("Diagnóstico", diagnostics())
        );
        return UiUtils.scrollable(root);
    }

    private GridPane applicationInfo() {
        AppPaths paths = context.appPaths();
        GridPane grid = infoGrid();
        addInfoRow(grid, 0, "Nome", "HFinance");
        addInfoRow(grid, 1, "Versão", AppVersion.VERSION);
        addInfoRow(grid, 2, "Sistema", System.getProperty("os.name"));
        addInfoRow(grid, 3, "Java Runtime", System.getProperty("java.version"));
        addInfoRow(grid, 4, "Diretório de dados", paths.dataDirectory().toAbsolutePath().toString());
        addInfoRow(grid, 5, "Banco de dados", paths.databasePath().toAbsolutePath().toString());
        addInfoRow(grid, 6, "Diretório de backups", paths.backupsDirectory().toAbsolutePath().toString());
        addInfoRow(grid, 7, "Diretório de logs", paths.logsDirectory().toAbsolutePath().toString());
        addInfoRow(grid, 8, "Diretório de exports", paths.exportsDirectory().toAbsolutePath().toString());
        return grid;
    }

    private VBox dataSafety() {
        Button backupNow = UiUtils.primaryButton("Fazer backup agora");
        backupNow.setOnAction(event -> createManualBackup());

        Button openData = UiUtils.secondaryButton("Abrir pasta de dados");
        openData.setOnAction(event -> openDirectory(context.appPaths().dataDirectory()));

        Button openBackups = UiUtils.secondaryButton("Abrir pasta de backups");
        openBackups.setOnAction(event -> openDirectory(context.appPaths().backupsDirectory()));

        Button openLogs = UiUtils.secondaryButton("Abrir pasta de logs");
        openLogs.setOnAction(event -> openDirectory(context.appPaths().logsDirectory()));

        FlowPane actions = new FlowPane(10, 10, backupNow, openData, openBackups, openLogs);
        actions.setPadding(new Insets(4, 0, 0, 0));

        return new VBox(10,
                UiUtils.helperText("Os dados do HFinance ficam no diretório do usuário. Backups manuais são preservados e backups automáticos protegem atualizações do banco."),
                lastBackupLabel,
                actions
        );
    }

    private VBox diagnostics() {
        Button exportDiagnostic = UiUtils.primaryButton("Exportar diagnóstico");
        exportDiagnostic.setOnAction(event -> exportDiagnostic());

        Button openExports = UiUtils.secondaryButton("Abrir pasta de exports");
        openExports.setOnAction(event -> openDirectory(context.appPaths().exportsDirectory()));

        FlowPane actions = new FlowPane(10, 10, exportDiagnostic, openExports);
        actions.setPadding(new Insets(4, 0, 0, 0));

        return new VBox(10,
                integrityLabel,
                schemaVersionLabel,
                UiUtils.helperText("O diagnóstico exporta metadados técnicos e últimas linhas do log. Dados financeiros sensíveis não são incluídos intencionalmente."),
                actions
        );
    }

    private void createManualBackup() {
        try {
            BackupResult result = context.backupService().createBackup(context.appPaths().databasePath(), BackupType.MANUAL);
            refreshStatus();
            Notification.success("""
                    Backup criado com sucesso.

                    Arquivo:
                    %s
                    """.formatted(result.fileName()));
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Falha ao criar backup manual.", ex);
            Notification.error("""
                    Não foi possível criar o backup agora.

                    Verifique se o HFinance tem permissão para acessar a pasta de dados.
                    """);
        }
    }

    private void exportDiagnostic() {
        try {
            Path diagnostic = context.diagnosticsService().exportDiagnostic();
            Notification.success("""
                    Diagnóstico exportado com sucesso.

                    Arquivo:
                    %s
                    """.formatted(diagnostic.getFileName()));
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Falha ao exportar diagnóstico pela interface.", ex);
            Notification.error("Não foi possível exportar o diagnóstico.");
        }
    }

    private void openDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
            if (!Desktop.isDesktopSupported()) {
                throw new IOException("Desktop não suportado neste ambiente.");
            }
            Desktop.getDesktop().open(directory.toFile());
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Falha ao abrir pasta " + directory.toAbsolutePath(), ex);
            Notification.error("Não foi possível abrir a pasta solicitada.");
        }
    }

    private void refreshStatus() {
        Path lastBackup = context.backupService().lastBackup();
        lastBackupLabel.setText("Último backup: " + formatBackup(lastBackup));

        DatabaseHealthResult health = Files.exists(context.appPaths().databasePath())
                ? context.healthChecker().check(context.appPaths().databasePath())
                : DatabaseHealthResult.unhealthy("Arquivo inexistente");
        integrityLabel.setText("Integridade do banco: " + health.integrityStatus());
        schemaVersionLabel.setText("Versão do schema: " + health.schemaVersion());
    }

    private String databaseExistsLabel() {
        return Files.exists(context.appPaths().databasePath()) ? "Disponível" : "Ausente";
    }

    private String formatBackup(Path backup) {
        if (backup == null) {
            return "nenhum backup encontrado";
        }
        try {
            return DATE_TIME_FORMATTER.format(Files.getLastModifiedTime(backup).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()) + " - " + backup.getFileName();
        } catch (IOException ex) {
            return backup.getFileName().toString();
        }
    }

    private GridPane infoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        return grid;
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");

        Label valueNode = UiUtils.helperText(value);
        valueNode.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(valueNode, Priority.ALWAYS);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
}
