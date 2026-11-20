package com.invoicelite.invoice;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(Money.of("50.00", "EUR"), total);
    }

    @Test
    void rejects_a_quantity_below_one() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new LineItem("MUG-01", "Enamel mug", MUG_PRICE, 0));

        assertEquals("Quantity must be at least 1, was 0", error.getMessage());
    }

    @Test
    void rejects_a_blank_sku() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(" ", "Enamel mug", MUG_PRICE, 1));
    }
}
