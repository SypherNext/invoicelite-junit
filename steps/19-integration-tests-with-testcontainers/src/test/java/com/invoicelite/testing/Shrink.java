package com.invoicelite.testing;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A very small shrinker. Starting from a failing list of lines, it keeps trying one-step-simpler
 * variants and moves to the first one that still fails, until no simpler variant fails.
 */
public final class Shrink {

    private Shrink() {
    }

    public static List<LineItem> lineList(List<LineItem> failing, Predicate<List<LineItem>> stillFails) {
        List<LineItem> current = failing;
        boolean shrunk = true;
        while (shrunk) {
            shrunk = false;
            for (List<LineItem> candidate : simplerVariants(current)) {
                if (stillFails.test(candidate)) {
                    current = candidate;
                    shrunk = true;
                    break;
                }
            }
        }
        return current;
    }

    /** One line removed, or one line with its price set to 0.01, halved or one cent lower, or one unit fewer. */
    private static List<List<LineItem>> simplerVariants(List<LineItem> lines) {
        List<List<LineItem>> variants = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            List<LineItem> without = new ArrayList<>(lines);
            without.remove(i);
            variants.add(without);
        }
        for (int i = 0; i < lines.size(); i++) {
            LineItem line = lines.get(i);
            long cents = line.unitPrice().amount().movePointRight(2).longValueExact();
            if (cents > 1) {
                variants.add(replace(lines, i, withPrice(line, 1)));
                variants.add(replace(lines, i, withPrice(line, cents / 2)));
                variants.add(replace(lines, i, withPrice(line, cents - 1)));
            }
            if (line.quantity() > 1) {
                variants.add(replace(lines, i, new LineItem(line.sku(), line.description(), line.unitPrice(), line.quantity() - 1)));
            }
        }
        return variants;
    }

    private static LineItem withPrice(LineItem line, long cents) {
        Money price = new Money(BigDecimal.valueOf(cents, 2), line.unitPrice().currency());
        return new LineItem(line.sku(), line.description(), price, line.quantity());
    }

    private static List<LineItem> replace(List<LineItem> lines, int index, LineItem replacement) {
        List<LineItem> copy = new ArrayList<>(lines);
        copy.set(index, replacement);
        return copy;
    }
}
