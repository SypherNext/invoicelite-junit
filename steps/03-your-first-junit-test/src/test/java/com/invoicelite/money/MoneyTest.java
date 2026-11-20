package com.invoicelite.money;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void addsTwoAmounts() {
        Money mug = Money.of("12.50", "EUR");
        Money pen = Money.of("6.40", "EUR");

        Money total = mug.plus(pen);

        assertEquals(Money.of("18.90", "EUR"), total);
    }

    @Test
    void multipliesByQuantity() {
        Money price = Money.of("4.99", "EUR");

        Money total = price.times(3);

        assertEquals(Money.of("14.97", "EUR"), total);
    }

    @Test
    void roundsHalfToEven() {
        assertEquals("12.34", Money.of("12.345", "EUR").amount().toPlainString());
        assertEquals("12.36", Money.of("12.355", "EUR").amount().toPlainString());
    }

    @Test
    void refusesToAddDifferentCurrencies() {
        Money euros = Money.of("10.00", "EUR");
        Money dollars = Money.of("10.00", "USD");

        assertThrows(IllegalArgumentException.class, () -> euros.plus(dollars));
    }
}
