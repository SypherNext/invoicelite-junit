package com.invoicelite.money;

import com.invoicelite.testing.WithLocale;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyFormatterDefaultLocaleTest {

    @Test
    @WithLocale("de-DE")
    void followsTheDefaultLocale() {
        assertThat(Locale.getDefault()).isEqualTo(Locale.GERMANY);
        assertThat(MoneyFormatter.forDefaultLocale().format(Money.of("1234.50", "EUR")))
                .isEqualTo("1.234,50\u00A0€");
    }

    @Test
    @WithLocale("en-US")
    void switchesWithTheDefaultLocale() {
        assertThat(MoneyFormatter.forDefaultLocale().format(Money.of("1234.50", "EUR")))
                .isEqualTo("€1,234.50");
    }
}
