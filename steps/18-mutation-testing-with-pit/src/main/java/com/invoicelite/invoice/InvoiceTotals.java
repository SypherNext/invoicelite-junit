package com.invoicelite.invoice;

import com.invoicelite.money.Money;

/**
 * The amounts printed at the bottom of an invoice.
 */
public record InvoiceTotals(Money subtotal, Money discount, Money tax, Money total) {
}
