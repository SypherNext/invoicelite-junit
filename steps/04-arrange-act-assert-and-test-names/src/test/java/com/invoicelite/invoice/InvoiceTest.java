package com.invoicelite.invoice;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("An invoice")
class InvoiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    @DisplayName("starts with no lines")
    void startsEmpty() {
        Invoice invoice = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);

        assertTrue(invoice.lines().isEmpty());
    }

    @Test
    @DisplayName("keeps lines in the order they were added")
    void keepsLineOrder() {
        Invoice invoice = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);
        LineItem mug = new LineItem("MUG-01", "Enamel mug", Money.of("12.50", "EUR"), 2);
        LineItem pen = new LineItem("PEN-10", "Gel pen pack", Money.of("6.40", "EUR"), 1);

        invoice.addLine(mug);
        invoice.addLine(pen);

        assertEquals(List.of(mug, pen), invoice.lines());
    }

    @Test
    @DisplayName("refuses a line in another currency")
    void refusesOtherCurrency() {
        Invoice invoice = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);
        LineItem dollarLine = new LineItem("MUG-01", "Enamel mug", Money.of("12.50", "USD"), 1);

        assertThrows(IllegalArgumentException.class, () -> invoice.addLine(dollarLine));
    }
}
