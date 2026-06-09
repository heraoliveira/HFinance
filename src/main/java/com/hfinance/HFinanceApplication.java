package com.hfinance;

import com.hfinance.core.config.AppPaths;
import com.hfinance.core.config.AppVersion;
import com.hfinance.core.config.HFinanceContext;
import com.hfinance.core.error.ErrorDialogService;
import com.hfinance.core.error.GlobalExceptionHandler;
import com.hfinance.core.logging.AppLogger;
import com.hfinance.ui.component.Notification;
import com.hfinance.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HFinanceApplication extends Application {
    private static final Logger LOGGER = AppLogger.logger(HFinanceApplication.class);

    @Override
    public void start(Stage stage) {
        AppPaths appPaths = AppPaths.createDefault();
        AppLogger.configure(appPaths.logsDirectory());
        GlobalExceptionHandler.install(appPaths);

        HFinanceContext context;
        try {
            context = HFinanceContext.create();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Falha crítica durante a inicialização.", ex);
            ErrorDialogService.showStartupError(appPaths.dataDirectory(), appPaths.backupsDirectory(),
                    appPaths.logsDirectory().resolve("hfinance.log"));
            return;
        }
        MainController mainController = new MainController(context);
        GlobalExceptionHandler.installUiNotifier(Notification::error);
        Scene scene = new Scene(mainController.getView(), 1280, 800);
        String css = getClass().getResource("/css/theme.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle(AppVersion.DISPLAY_NAME);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        stage.setMinWidth(1180);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        LOGGER.info("HFinance encerrado.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
