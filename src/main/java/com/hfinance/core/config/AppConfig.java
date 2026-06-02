package com.hfinance.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppConfig {
    public static final String APP_NAME = "HFinance";
    public static final String APP_SUBTITLE = "Solução desktop para finanças pessoais";

    private AppConfig() {
    }

    public static Path dataDirectory() {
        Path projectData = Path.of("data");
        if (isWritableDirectory(projectData)) {
            return projectData;
        }

        String appData = System.getenv("APPDATA");
        Path fallback = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), ".hfinance")
                : Path.of(appData, APP_NAME);
        if (isWritableDirectory(fallback)) {
            return fallback;
        }
        return projectData;
    }

    private static boolean isWritableDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
            Path probe = Files.createTempFile(directory, "write-test", ".tmp");
            Files.deleteIfExists(probe);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
