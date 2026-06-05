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
        assertThat(css).contains(".modal-backdrop");
        assertThat(css).contains(".toast-success");
        assertThat(css).contains(".category-swatch");
    }

    @Test
    void iconAssetsExistAndPackagingScriptUsesIco() throws Exception {
        assertThat(Path.of("src/main/resources/images/app-icon.svg")).exists();
        assertThat(Path.of("src/main/resources/images/app-icon.png")).exists();
        assertThat(Path.of("src/main/resources/images/app-icon.ico")).exists();

        String script = Files.readString(Path.of("scripts/package-windows.ps1"), StandardCharsets.UTF_8);
        assertThat(script).contains("src\\main\\resources\\images\\app-icon.ico");
        assertThat(script).contains("$AppVersion = \"1.2.1\"");
        assertThat(script).contains("Remove-FileInsideProject $LooseIconPath");
    }

    @Test
    void commonNotificationsUseInternalComponents() throws Exception {
        String notification = Files.readString(Path.of("src/main/java/com/hfinance/ui/component/Notification.java"),
                StandardCharsets.UTF_8);

        assertThat(notification).doesNotContain("Alert");
        assertThat(notification).contains("modal-backdrop");
        assertThat(notification).contains("confirmRecurringEdit");
    }

    @Test
    void formsDoNotExposeRedundantNewButtonsOrRecurrenceEndDate() throws Exception {
        String controllers = Files.readString(Path.of("src/main/java/com/hfinance/ui/controller/AccountController.java"),
                StandardCharsets.UTF_8)
                + Files.readString(Path.of("src/main/java/com/hfinance/ui/controller/TransactionController.java"),
                StandardCharsets.UTF_8)
                + Files.readString(Path.of("src/main/java/com/hfinance/ui/controller/CategoryController.java"),
                StandardCharsets.UTF_8)
                + Files.readString(Path.of("src/main/java/com/hfinance/ui/controller/BudgetController.java"),
                StandardCharsets.UTF_8)
                + Files.readString(Path.of("src/main/java/com/hfinance/ui/controller/GoalController.java"),
                StandardCharsets.UTF_8);

        assertThat(controllers)
                .doesNotContain("Nova conta")
                .doesNotContain("Nova transação")
                .doesNotContain("Nova categoria")
                .doesNotContain("Novo orçamento")
                .doesNotContain("Nova meta")
                .doesNotContain("recurrenceEndPicker");
    }
}
