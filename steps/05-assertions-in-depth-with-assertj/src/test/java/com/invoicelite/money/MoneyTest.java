package com.invoicelite.money;

import org.junit.jupiter.api.Test;

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
    void keepsTwoDecimalPlaces() {
        Money money = Money.of("18.9", "EUR");

        assertThat(money.amount()).isEqualByComparingTo("18.90");
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void roundsHalfToEven() {
        assertThat(Money.of("12.345", "EUR").amount()).hasToString("12.34");
        assertThat(Money.of("12.355", "EUR").amount()).hasToString("12.36");
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
