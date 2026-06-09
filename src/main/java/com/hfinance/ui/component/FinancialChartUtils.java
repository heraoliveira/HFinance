package com.hfinance.ui.component;

import com.hfinance.core.format.MoneyFormatter;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public final class FinancialChartUtils {
    private static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");

    private FinancialChartUtils() {
    }

    public static void configureMonthlyBarChart(BarChart<String, Number> chart, String title) {
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setCategoryGap(18);
        chart.setBarGap(4);

        if (chart.getXAxis() instanceof CategoryAxis categoryAxis) {
            categoryAxis.setLabel("Mês");
        }
        if (chart.getYAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setLabel("Valor (R$)");
            numberAxis.setForceZeroInRange(true);
            numberAxis.setTickLabelFormatter(new StringConverter<>() {
                @Override
                public String toString(Number value) {
                    return formatAxisMoney(value);
                }

                @Override
                public Number fromString(String value) {
                    return 0;
                }
            });
        }
    }

    public static void installBarTooltips(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            installTooltip(data, barTooltipText(series.getName(), data.getXValue(), data.getYValue()));
        }
    }

    public static void populatePieChart(PieChart chart, String title, Map<String, BigDecimal> values) {
        chart.setTitle(title);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelsVisible(true);
        chart.setAnimated(false);

        BigDecimal total = values.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        values.forEach((label, value) -> {
            PieChart.Data data = new PieChart.Data(pieLegendLabel(label, value, total), value.doubleValue());
            chart.getData().add(data);
            installTooltip(data, pieTooltipText(label, value, total));
        });
    }

    public static VBox emptyState(String message, String guidance) {
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("chart-empty-title");
        messageLabel.setWrapText(true);

        Label guidanceLabel = new Label(guidance);
        guidanceLabel.getStyleClass().add("chart-empty-message");
        guidanceLabel.setWrapText(true);

        VBox empty = new VBox(6, messageLabel, guidanceLabel);
        empty.getStyleClass().add("chart-empty-state");
        empty.setAlignment(Pos.CENTER_LEFT);
        empty.setMaxHeight(Region.USE_PREF_SIZE);
        return empty;
    }

    public static String formatPercentage(BigDecimal value, BigDecimal total) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        BigDecimal safeTotal = total == null ? BigDecimal.ZERO : total;
        BigDecimal percentage = safeTotal.signum() == 0
                ? BigDecimal.ZERO
                : safeValue.multiply(BigDecimal.valueOf(100))
                .divide(safeTotal, 2, RoundingMode.HALF_UP);

        NumberFormat format = NumberFormat.getNumberInstance(BRAZIL);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(percentage) + "%";
    }

    public static String barTooltipText(String series, String category, Number value) {
        return "%s\nMês: %s\nValor: %s".formatted(
                series,
                category,
                MoneyFormatter.format(toBigDecimal(value)));
    }

    public static String pieTooltipText(String label, BigDecimal value, BigDecimal total) {
        return "%s\nValor: %s\nParticipação: %s".formatted(
                label,
                MoneyFormatter.format(value),
                formatPercentage(value, total));
    }

    public static String pieLegendLabel(String label, BigDecimal value, BigDecimal total) {
        return "%s · %s".formatted(label, formatPercentage(value, total));
    }

    private static String formatAxisMoney(Number value) {
        BigDecimal amount = toBigDecimal(value);
        NumberFormat format = NumberFormat.getNumberInstance(BRAZIL);
        format.setMaximumFractionDigits(0);
        return "R$ " + format.format(amount);
    }

    private static BigDecimal toBigDecimal(Number value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private static void installTooltip(XYChart.Data<String, Number> data, String text) {
        if (data.getNode() != null) {
            installTooltip(data.getNode(), text);
        }
        data.nodeProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {
                installTooltip(newNode, text);
            }
        });
    }

    private static void installTooltip(PieChart.Data data, String text) {
        if (data.getNode() != null) {
            installTooltip(data.getNode(), text);
        }
        data.nodeProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {
                installTooltip(newNode, text);
            }
        });
    }

    private static void installTooltip(Node node, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.getStyleClass().add("financial-chart-tooltip");
        tooltip.setShowDelay(Duration.millis(180));
        tooltip.setShowDuration(Duration.seconds(12));
        Tooltip.install(node, tooltip);
    }
}
