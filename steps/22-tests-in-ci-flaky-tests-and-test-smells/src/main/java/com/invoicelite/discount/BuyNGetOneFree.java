package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.util.Comparator;
import java.util.List;

/**
 * For every paidUnits units of one SKU, the next unit is free: "buy 3 mugs, get the 4th free".
 * When the same SKU appears at different prices, the cheapest units are the free ones.
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
        List<LineItem> cheapestFirst = lines.stream()
                .filter(line -> line.sku().equals(sku))
                .sorted(Comparator.comparing(line -> line.unitPrice().amount()))
                .toList();
        int units = cheapestFirst.stream().mapToInt(LineItem::quantity).sum();
        int freeUnitsLeft = units / (paidUnits + 1);

        Money discount = Money.zero(subtotal.currency());
        for (LineItem line : cheapestFirst) {
            int free = Math.min(freeUnitsLeft, line.quantity());
            discount = discount.plus(line.unitPrice().times(free));
            freeUnitsLeft -= free;
        }
        return discount;
    }
}
