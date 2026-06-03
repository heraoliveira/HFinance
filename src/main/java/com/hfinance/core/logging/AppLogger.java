package com.hfinance.core.logging;

import com.hfinance.core.config.AppVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class AppLogger {
    private static boolean configured;

    private AppLogger() {
    }

    public static synchronized void configure(Path logsDirectory) {
        if (configured) {
            return;
        }
        try {
            Files.createDirectories(logsDirectory);
            Logger rootLogger = Logger.getLogger("com.hfinance");
            rootLogger.setUseParentHandlers(false);
            rootLogger.setLevel(Level.INFO);
            if (Boolean.getBoolean("hfinance.disableFileLogging")) {
                configured = true;
                return;
            }
            rootLogger.addHandler(fileHandler(logsDirectory.resolve("hfinance.log")));
            rootLogger.addHandler(fileHandler(logsDirectory.resolve("hfinance-" + LocalDate.now() + ".log")));
            FileHandler errorHandler = fileHandler(logsDirectory.resolve("hfinance-error.log"));
            errorHandler.setFilter(errorFilter());
            rootLogger.addHandler(errorHandler);
            configured = true;
            logger(AppLogger.class).info(() -> "HFinance " + AppVersion.VERSION
                    + " iniciado. Sistema=" + System.getProperty("os.name")
                    + ", Java=" + System.getProperty("java.version"));
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível inicializar os logs do HFinance.", ex);
        }
    }

    public static Logger logger(Class<?> type) {
        return Logger.getLogger(type.getName());
    }

    private static FileHandler fileHandler(Path path) throws IOException {
        FileHandler handler = new FileHandler(path.toString(), true);
        handler.setEncoding("UTF-8");
        handler.setFormatter(new SingleLineFormatter());
        handler.setLevel(Level.ALL);
        return handler;
    }

    private static Filter errorFilter() {
        return record -> record.getLevel().intValue() >= Level.SEVERE.intValue();
    }

    private static final class SingleLineFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String thrown = "";
            if (record.getThrown() != null) {
                thrown = System.lineSeparator() + stackTrace(record.getThrown());
            }
            return "%1$tF %1$tT %2$s %3$s - %4$s%5$s%n".formatted(
                    record.getMillis(),
                    record.getLevel(),
                    record.getLoggerName(),
                    formatMessage(record),
                    thrown
            );
        }

        private String stackTrace(Throwable throwable) {
            StringBuilder builder = new StringBuilder(throwable.toString());
            for (StackTraceElement element : throwable.getStackTrace()) {
                builder.append(System.lineSeparator()).append("\tat ").append(element);
            }
            if (throwable.getCause() != null) {
                builder.append(System.lineSeparator()).append("Caused by: ").append(stackTrace(throwable.getCause()));
            }
            return builder.toString();
        }
    }
}
