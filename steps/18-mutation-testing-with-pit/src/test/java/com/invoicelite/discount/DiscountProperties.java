package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;
import com.invoicelite.testing.InvoiceGenerators;
import com.invoicelite.testing.Shrink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

/**
 * Every discount policy, on many generated invoices: a discount is never negative and never more than the subtotal.
 */
class DiscountProperties {

    /** Breaking invoices are rare, so each policy is checked against many generated invoices. */
    private static final int INVOICES_PER_POLICY = 2_000;

    static Stream<Arguments> policies() {
        return Stream.of(
                argumentSet("no discount", DiscountPolicy.none()),
                argumentSet("10 percent", new PercentageDiscount(new BigDecimal("10"))),
                argumentSet("tiered", TieredDiscount.standard()),
                argumentSet("buy 3 mugs, get 1 free", new BuyNGetOneFree("MUG-01", 3)));
    }

    @ParameterizedTest
    @MethodSource("policies")
    void aDiscountIsNeverNegativeNorMoreThanTheSubtotal(DiscountPolicy policy) {
        InvoiceGenerators generate = InvoiceGenerators.fromRunSeed();
        for (int sample = 1; sample <= INVOICES_PER_POLICY; sample++) {
            List<LineItem> lines = generate.lines(0, 8);
            if (outOfRange(policy, lines)) {
                List<LineItem> smallest = Shrink.lineList(lines, candidate -> outOfRange(policy, candidate));
                fail("""
                        discount outside 0 to subtotal on sample %d (seed %d)
                        generated: %s
                        shrunk to: %s
                        discount %s, subtotal %s"""
                        .formatted(sample, InvoiceGenerators.SEED, lines, smallest,
                                policy.discountFor(smallest, InvoiceGenerators.subtotalOf(smallest)).amount(),
                                InvoiceGenerators.subtotalOf(smallest).amount()));
            }
        }
    }

    private static boolean outOfRange(DiscountPolicy policy, List<LineItem> lines) {
        Money subtotal = InvoiceGenerators.subtotalOf(lines);
        Money discount = policy.discountFor(lines, subtotal);
        return discount.amount().signum() < 0 || discount.amount().compareTo(subtotal.amount()) > 0;
    }
}
