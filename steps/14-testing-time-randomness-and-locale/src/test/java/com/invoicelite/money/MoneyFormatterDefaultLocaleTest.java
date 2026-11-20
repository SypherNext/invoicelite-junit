package com.invoicelite.money;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Changes the JVM's default locale, so it must put the old one back afterwards.
 */
class MoneyFormatterDefaultLocaleTest {

    private Locale originalLocale;

    @BeforeEach
    void switchToGerman() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    void followsTheDefaultLocale() {
        assertThat(MoneyFormatter.forDefaultLocale().format(Money.of("1234.50", "EUR")))
                .isEqualTo("1.234,50\u00A0€");
    }
}
