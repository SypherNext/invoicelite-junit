package com.invoicelite.money;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void addsTwoAmounts() {
        Money mug = Money.of("12.50", "EUR");
        Money pen = Money.of("6.40", "EUR");

        Money total = mug.plus(pen);

        assertThat(total).isEqualTo(Money.of("18.90", "EUR"));
    }

    @Test
    void multipliesByQuantity() {
        Money price = Money.of("4.99", "EUR");

        Money total = price.times(3);

        assertThat(total).isEqualTo(Money.of("14.97", "EUR"));
    }

    @Test
    void subtractsAnAmount() {
        Money total = Money.of("62.80", "EUR");

        Money afterDiscount = total.minus(Money.of("6.28", "EUR"));

        assertThat(afterDiscount).isEqualTo(Money.of("56.52", "EUR"));
    }

    @Test
    void negatesAnAmount() {
        Money total = Money.of("62.80", "EUR");

        Money negated = total.negated();

        assertThat(negated).isEqualTo(Money.of("-62.80", "EUR"));
    }

    @Test
    void takesAPercentage() {
        Money subtotal = Money.of("62.80", "EUR");

        assertThat(subtotal.percent(new BigDecimal("10"))).isEqualTo(Money.of("6.28", "EUR"));
    }

    @Test
    void keepsTwoDecimalPlaces() {
        Money money = Money.of("18.9", "EUR");

        assertThat(money.amount()).isEqualByComparingTo("18.90");
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{0} is stored as {1}")
    @CsvSource({
            "12.345, 12.34",
            "12.355, 12.36",
            "0.005,  0.00",
            "0.015,  0.02",
            "-1.005, -1.00"
    })
    void roundsHalfToEven(String input, String stored) {
        assertThat(Money.of(input, "EUR").amount()).hasToString(stored);
    }

    @Test
    void refusesToAddDifferentCurrencies() {
        Money euros = Money.of("10.00", "EUR");
        Money dollars = Money.of("10.00", "USD");

        assertThatThrownBy(() -> euros.plus(dollars))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot combine EUR with USD");
    }

    @Test
    void refusesAnUnknownCurrencyCode() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Money.of("1.00", "XYZ"));
    }
}
