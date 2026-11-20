package com.invoicelite.invoice;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceCalculatorTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private final InvoiceCalculator calculator = new InvoiceCalculator();

    @Test
    void subtotal_of_an_invoice_without_lines_is_zero() {
        Invoice invoice = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);

        Money subtotal = calculator.subtotal(invoice);

        assertEquals(Money.of("0.00", "EUR"), subtotal);
    }

    @Test
    void subtotal_adds_up_every_line_total() {
        Invoice invoice = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);
        invoice.addLine(new LineItem("MUG-01", "Enamel mug", Money.of("12.50", "EUR"), 4));
        invoice.addLine(new LineItem("PEN-10", "Gel pen pack", Money.of("6.40", "EUR"), 2));

        Money subtotal = calculator.subtotal(invoice);

        assertEquals(Money.of("62.80", "EUR"), subtotal);
    }
}
