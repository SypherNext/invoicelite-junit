package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.util.List;

/**
 * For every paidUnits units of one SKU, the next unit is free: "buy 3 mugs, get the 4th free".
 */
public final class BuyNGetOneFree implements DiscountPolicy {

    private final String sku;
    private final int paidUnits;

    public BuyNGetOneFree(String sku, int paidUnits) {
        if (paidUnits < 1) {
            throw new IllegalArgumentException("Paid units must be at least 1, was " + paidUnits);
        }
        this.sku = sku;
        this.paidUnits = paidUnits;
    }

    @Override
    public Money discountFor(List<LineItem> lines, Money subtotal) {
        List<LineItem> matching = lines.stream()
                .filter(line -> line.sku().equals(sku))
                .toList();
        if (matching.isEmpty()) {
            return Money.zero(subtotal.currency());
        }
        int units = matching.stream().mapToInt(LineItem::quantity).sum();
        int freeUnits = units / (paidUnits + 1);
        return matching.getFirst().unitPrice().times(freeUnits);
    }
}
