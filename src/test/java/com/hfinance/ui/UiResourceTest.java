package com.hfinance.ui;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UiResourceTest {
    @Test
    void themeContainsDatePickerPopupRules() throws Exception {
        String css = Files.readString(Path.of("src/main/resources/css/theme.css"), StandardCharsets.UTF_8);

        assertThat(css).contains(".date-picker-popup");
        assertThat(css).contains(".date-picker-popup .month-year-pane");
        assertThat(css).contains(".date-picker-popup .spinner-label");
        assertThat(css).contains(".date-picker-popup .selected");
        assertThat(css).contains("#00E5FF");
        assertThat(css).contains("#FFD700");
    }

    @Test
    void iconAssetsExistAndPackagingScriptUsesIco() throws Exception {
        assertThat(Path.of("src/main/resources/images/app-icon.svg")).exists();
        assertThat(Path.of("src/main/resources/images/app-icon.png")).exists();
        assertThat(Path.of("src/main/resources/images/app-icon.ico")).exists();

        String script = Files.readString(Path.of("scripts/package-windows.ps1"), StandardCharsets.UTF_8);
        assertThat(script).contains("src\\main\\resources\\images\\app-icon.ico");
        assertThat(script).contains("$AppVersion = \"1.2.0\"");
    }
}
