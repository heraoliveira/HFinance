package com.hfinance;

import com.hfinance.core.config.HFinanceContext;
import com.hfinance.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HFinanceApplication extends Application {
    @Override
    public void start(Stage stage) {
        HFinanceContext context = HFinanceContext.create();
        MainController mainController = new MainController(context);
        Scene scene = new Scene(mainController.getView(), 1280, 800);
        String css = getClass().getResource("/css/theme.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("HFinance v1.0.0");
        stage.setMinWidth(1180);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
