package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * The bigger the subtotal, the bigger the percentage: the highest tier the subtotal reaches applies.
 */
public final class TieredDiscount implements DiscountPolicy {

    /** From this subtotal on (inclusive), take this percentage off. */
    public record Tier(BigDecimal from, BigDecimal percent) {
    }

    private final List<Tier> tiers;

    public TieredDiscount(List<Tier> tiers) {
        this.tiers = List.copyOf(tiers);
    }

    public static TieredDiscount standard() {
        return new TieredDiscount(List.of(
                new Tier(new BigDecimal("1000.00"), new BigDecimal("5")),
                new Tier(new BigDecimal("5000.00"), new BigDecimal("10"))));
    }

    @Override
    public Money discountFor(List<LineItem> lines, Money subtotal) {
        return tiers.stream()
                .filter(tier -> subtotal.amount().compareTo(tier.from()) >= 0)
                .max(Comparator.comparing(Tier::from))
                .map(tier -> subtotal.percent(tier.percent()))
                .orElse(Money.zero(subtotal.currency()));
    }
}
