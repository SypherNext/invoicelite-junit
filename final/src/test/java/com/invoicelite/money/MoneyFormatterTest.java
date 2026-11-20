package com.invoicelite.money;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyFormatterTest {

    private static final Money AMOUNT = Money.of("1234.50", "EUR");
    private static final String NO_BREAK_SPACE = "\u00A0";
    private static final String NARROW_NO_BREAK_SPACE = "\u202F";

    @Test
    void writesEurosTheGermanWay() {
        String text = new MoneyFormatter(Locale.GERMANY).format(AMOUNT);

        assertThat(text).isEqualTo("1.234,50" + NO_BREAK_SPACE + "€");
    }

    @Test
    void writesEurosTheAmericanWay() {
        assertThat(new MoneyFormatter(Locale.US).format(AMOUNT)).isEqualTo("€1,234.50");
    }

    @Test
    void writesEurosTheFrenchWay() {
        String text = new MoneyFormatter(Locale.FRANCE).format(AMOUNT);

        assertThat(text).isEqualTo("1" + NARROW_NO_BREAK_SPACE + "234,50" + NO_BREAK_SPACE + "€");
    }
}
