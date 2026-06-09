package com.hfinance.ui;

import com.hfinance.ui.component.FinancialChartUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialChartUtilsTest {
    @Test
    void formatsPercentagesInBrazilianPortuguese() {
        assertThat(FinancialChartUtils.formatPercentage(new BigDecimal("25"), new BigDecimal("100")))
                .isEqualTo("25,00%");
        assertThat(FinancialChartUtils.formatPercentage(new BigDecimal("10"), BigDecimal.ZERO))
                .isEqualTo("0,00%");
    }

    @Test
    void buildsFinancialTooltipTexts() {
        assertThat(FinancialChartUtils.barTooltipText("Receitas", "06/2026", new BigDecimal("1234.56")))
                .contains("Receitas", "Mês: 06/2026", "Valor:", "1.234,56");

        assertThat(FinancialChartUtils.pieTooltipText(
                "Alimentação",
                new BigDecimal("250"),
                new BigDecimal("1000")))
                .contains("Alimentação", "Valor:", "250,00", "Participação: 25,00%");
    }

    @Test
    void buildsPieLegendWithPercentage() {
        assertThat(FinancialChartUtils.pieLegendLabel(
                "Pix",
                new BigDecimal("375"),
                new BigDecimal("1000")))
                .isEqualTo("Pix · 37,50%");
    }
}
