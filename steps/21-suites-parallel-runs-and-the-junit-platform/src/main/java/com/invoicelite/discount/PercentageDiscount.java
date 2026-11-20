package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.math.BigDecimal;
import java.util.List;

/**
 * Takes a fixed percentage off the subtotal, for example 10 percent for a loyal customer.
 */
public final class PercentageDiscount implements DiscountPolicy {

    private final BigDecimal percent;

    public PercentageDiscount(BigDecimal percent) {
        if (percent.signum() < 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percent must be between 0 and 100, was " + percent);
        }
        this.percent = percent;
    }

    @Override
    public Money discountFor(List<LineItem> lines, Money subtotal) {
        return subtotal.percent(percent);
    }
}
