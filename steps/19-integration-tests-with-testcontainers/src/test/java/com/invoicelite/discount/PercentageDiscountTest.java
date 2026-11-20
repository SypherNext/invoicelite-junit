package com.invoicelite.discount;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"0", "100"})
    void acceptsZeroAndOneHundredPercent(String percent) {
        DiscountPolicy policy = new PercentageDiscount(new BigDecimal(percent));

        assertThat(policy.discountFor(List.of(), Money.of("80.00", "EUR")))
                .isEqualTo(Money.of("80.00", "EUR").percent(new BigDecimal(percent)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "100.01", "150"})
    void refusesPercentagesOutsideZeroToOneHundred(String percent) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PercentageDiscount(new BigDecimal(percent)))
                .withMessage("Percent must be between 0 and 100, was " + percent);
    }
}
