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
        assertThat(css).contains(".toast-error");
        assertThat(css).contains(".toast-warning");
        assertThat(css).contains(".toast-info");
        assertThat(css).contains(".chart-empty-state");
        assertThat(css).contains(".financial-chart-tooltip");
        assertThat(css).contains(".category-swatch");
    }

    @Test
    void iconAssetsExistAndPackagingScriptUsesIco() throws Exception {
        assertThat(Path.of("src/main/resources/images/app-icon.svg")).exists();
        assertThat(Path.of("src/main/resources/images/app-icon.png")).exists();
        assertThat(Path.of("src/main/resources/images/app-icon.ico")).exists();

        String script = Files.readString(Path.of("scripts/package-windows.ps1"), StandardCharsets.UTF_8);
        assertThat(script).contains("src\\main\\resources\\images\\app-icon.ico");
        assertThat(script).contains("$AppVersion = \"1.2.2\"");
        assertThat(script).contains("Remove-FileInsideProject $LooseIconPath");
    }

    @Test
    void commonNotificationsUseInternalComponents() throws Exception {
        String notification = Files.readString(Path.of("src/main/java/com/hfinance/ui/component/Notification.java"),
                StandardCharsets.UTF_8);

        assertThat(notification).doesNotContain("Alert");
        assertThat(notification).contains("modal-backdrop");
        assertThat(notification).contains("confirmRecurringEdit");
        assertThat(notification).contains("public static void warning");
        assertThat(notification).contains("public static void info");
        assertThat(notification).contains("toastStack");
        assertThat(notification).contains("setMaxHeight(Region.USE_PREF_SIZE)");
        assertThat(notification).contains("setDefaultButton");
        assertThat(notification).contains("setCancelButton");
        assertThat(notification).contains("KeyCode.ESCAPE");
        assertThat(notification).contains("KeyCode.ENTER");
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

    @Test
    void financialChartsExposeTitlesAxesTooltipsAndEmptyStates() throws Exception {
        String helper = Files.readString(
                Path.of("src/main/java/com/hfinance/ui/component/FinancialChartUtils.java"),
                StandardCharsets.UTF_8);
        String dashboard = Files.readString(
                Path.of("src/main/java/com/hfinance/ui/controller/DashboardController.java"),
                StandardCharsets.UTF_8);
        String reports = Files.readString(
                Path.of("src/main/java/com/hfinance/ui/controller/ReportController.java"),
                StandardCharsets.UTF_8);

        assertThat(helper)
                .contains("Mês")
                .contains("Valor (R$)")
                .contains("setLegendSide(Side.BOTTOM)")
                .contains("Tooltip.install")
                .contains("formatPercentage")
                .contains("chart-empty-state");
        assertThat(dashboard)
                .contains("Receitas e despesas dos últimos 6 meses")
                .contains("Despesas por categoria no mês atual");
        assertThat(reports)
                .contains("Receitas e despesas por mês")
                .contains("Despesas por categoria no período")
                .contains("Transações por método de pagamento");
    }

    @Test
    void versionIsSynchronizedAtOneTwoTwo() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        String appVersion = Files.readString(
                Path.of("src/main/java/com/hfinance/core/config/AppVersion.java"),
                StandardCharsets.UTF_8);
        String readme = Files.readString(Path.of("README.md"), StandardCharsets.UTF_8);

        assertThat(pom).contains("<version>1.2.2</version>");
        assertThat(appVersion).contains("VERSION = \"1.2.2\"");
        assertThat(readme).contains("Versão atual: **1.2.2**.");
    }
}
