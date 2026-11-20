package com.invoicelite.invoice;

import com.invoicelite.money.Money;

import java.util.Currency;

/**
 * Small factory methods that keep test data short and readable.
 */
public final class TestInvoices {

    public static final Currency EUR = Currency.getInstance("EUR");

    private TestInvoices() {
    }

    public static Invoice draftFor(String countryCode) {
        return new Invoice("INV-2026-0001", "Acme GmbH", countryCode, EUR);
    }

    public static LineItem line(String sku, String unitPrice, int quantity) {
        return new LineItem(sku, sku + " item", Money.of(unitPrice, "EUR"), quantity);
    }
}
