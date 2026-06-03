package com.hfinance.core.config;

import java.nio.file.Path;

public final class AppConfig {
    public static final String APP_NAME = "HFinance";
    public static final String APP_SUBTITLE = "Solução desktop para finanças pessoais";

    private AppConfig() {
    }

    public static Path dataDirectory() {
        return AppPaths.createDefault().dataDirectory();
    }
}
