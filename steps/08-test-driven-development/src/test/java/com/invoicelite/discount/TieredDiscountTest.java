package com.invoicelite.discount;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TieredDiscountTest {

    private final DiscountPolicy tiers = TieredDiscount.standard();

    @Test
    void noDiscountBelowTheFirstTier() {
        assertThat(tiers.discountFor(List.of(), Money.of("999.99", "EUR")))
                .isEqualTo(Money.of("0.00", "EUR"));
    }

    @Test
    void fivePercentFromOneThousand() {
        assertThat(tiers.discountFor(List.of(), Money.of("1000.00", "EUR")))
                .isEqualTo(Money.of("50.00", "EUR"));
    }

    @Test
    void tenPercentFromFiveThousand() {
        assertThat(tiers.discountFor(List.of(), Money.of("6000.00", "EUR")))
                .isEqualTo(Money.of("600.00", "EUR"));
    }
}
