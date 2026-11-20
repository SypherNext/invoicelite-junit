package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.util.List;

/**
 * Decides how much is taken off an invoice before tax.
 */
public interface DiscountPolicy {

    Money discountFor(List<LineItem> lines, Money subtotal);

    static DiscountPolicy none() {
        return (lines, subtotal) -> Money.zero(subtotal.currency());
    }
}
