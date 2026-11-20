package com.invoicelite.invoice;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LineItemTest {

    private static final Money MUG_PRICE = Money.of("12.50", "EUR");

    @Test
    void total_is_unit_price_times_quantity() {
        // Arrange
        LineItem mugs = new LineItem("MUG-01", "Enamel mug", MUG_PRICE, 4);

        // Act
        Money total = mugs.total();

        // Assert
        assertThat(total).isEqualTo(Money.of("50.00", "EUR"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void rejects_a_quantity_below_one(int quantity) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LineItem("MUG-01", "Enamel mug", MUG_PRICE, quantity))
                .withMessage("Quantity must be at least 1, was " + quantity);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejects_a_missing_or_blank_sku(String sku) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LineItem(sku, "Enamel mug", MUG_PRICE, 1))
                .withMessage("SKU must not be blank");
    }
}
