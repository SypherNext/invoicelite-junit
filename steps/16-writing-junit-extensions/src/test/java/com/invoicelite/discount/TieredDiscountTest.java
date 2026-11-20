package com.invoicelite.discount;

import com.invoicelite.money.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TieredDiscountTest {

    private final DiscountPolicy tiers = TieredDiscount.standard();

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            subtotal, discount
            0.00,     0.00
            999.99,   0.00
            1000.00,  50.00
            4999.99,  250.00
            5000.00,  500.00
            6000.00,  600.00
            """)
    void appliesTheHighestTierReached(String subtotal, String discount) {
        assertThat(tiers.discountFor(List.of(), Money.of(subtotal, "EUR")))
                .isEqualTo(Money.of(discount, "EUR"));
    }
}
