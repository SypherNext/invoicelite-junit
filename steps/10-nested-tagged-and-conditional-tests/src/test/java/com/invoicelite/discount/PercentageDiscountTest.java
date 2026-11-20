package com.invoicelite.discount;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PercentageDiscountTest {

    @Test
    void takesThePercentageOffTheSubtotal() {
        DiscountPolicy tenPercent = new PercentageDiscount(new BigDecimal("10"));

        Money discount = tenPercent.discountFor(List.of(), Money.of("62.80", "EUR"));

        assertThat(discount).isEqualTo(Money.of("6.28", "EUR"));
    }

    @Test
    void refusesMoreThanOneHundredPercent() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PercentageDiscount(new BigDecimal("150")))
                .withMessage("Percent must be between 0 and 100, was 150");
    }
}
