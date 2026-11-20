package com.invoicelite.discount;

import com.invoicelite.money.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

/**
 * Rules that every discount policy must follow, checked against each one.
 */
class EveryDiscountPolicyTest {

    static Stream<Arguments> policies() {
        return Stream.of(
                argumentSet("no discount", DiscountPolicy.none()),
                argumentSet("10 percent", new PercentageDiscount(new BigDecimal("10"))),
                argumentSet("tiered", TieredDiscount.standard()),
                argumentSet("buy 3 mugs, get 1 free", new BuyNGetOneFree("MUG-01", 3)));
    }

    @ParameterizedTest
    @MethodSource("policies")
    void givesNothingOffAnEmptyInvoice(DiscountPolicy policy) {
        Money discount = policy.discountFor(List.of(), Money.of("0.00", "EUR"));

        assertThat(discount).isEqualTo(Money.of("0.00", "EUR"));
    }
}
