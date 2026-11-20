package com.invoicelite.invoice;

import com.invoicelite.discount.DiscountPolicy;
import com.invoicelite.money.Money;
import com.invoicelite.tax.TaxCalculator;

import java.util.Objects;

/**
 * Works out the amounts on an invoice: subtotal, discount, tax and total.
 */
public final class InvoiceCalculator {

    private final DiscountPolicy discountPolicy;
    private final TaxCalculator taxCalculator;

    public InvoiceCalculator(DiscountPolicy discountPolicy, TaxCalculator taxCalculator) {
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "taxCalculator");
    }

    public Money subtotal(Invoice invoice) {
        Money subtotal = Money.zero(invoice.currency());
        for (LineItem line : invoice.lines()) {
            subtotal = subtotal.plus(line.total());
        }
        return subtotal;
    }

    public InvoiceTotals totals(Invoice invoice) {
        Money subtotal = subtotal(invoice);
        Money discount = discountPolicy.discountFor(invoice.lines(), subtotal);
        Money tax = taxCalculator.taxFor(invoice, discount);
        Money total = subtotal.minus(discount).plus(tax);
        return new InvoiceTotals(subtotal, discount, tax, total);
    }

    /** What the customer still has to pay: the total less every payment so far. */
    public Money amountDue(Invoice invoice) {
        return totals(invoice).total().minus(invoice.amountPaid());
    }
}
