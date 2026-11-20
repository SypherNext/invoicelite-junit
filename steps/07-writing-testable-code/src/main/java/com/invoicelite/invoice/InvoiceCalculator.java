package com.invoicelite.invoice;

import com.invoicelite.money.Money;

/**
 * Works out the amounts on an invoice.
 */
public final class InvoiceCalculator {

    public Money subtotal(Invoice invoice) {
        Money subtotal = Money.zero(invoice.currency());
        for (LineItem line : invoice.lines()) {
            subtotal = subtotal.plus(line.total());
        }
        return subtotal;
    }
}
