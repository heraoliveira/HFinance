package com.hfinance.core.error;

import com.hfinance.core.config.AppPaths;
import com.hfinance.core.logging.AppLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class GlobalExceptionHandler {
    private static final Logger LOGGER = AppLogger.logger(GlobalExceptionHandler.class);
    private static AppPaths appPaths;

    private GlobalExceptionHandler() {
    }

    public static void install(AppPaths paths) {
        appPaths = paths;
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> handle(throwable));
    }

    public static void handle(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Erro inesperado não tratado.", throwable);
        if (appPaths != null) {
            ErrorDialogService.showUnexpectedError(appPaths.logsDirectory().resolve("hfinance.log"));
        }
    }
}
