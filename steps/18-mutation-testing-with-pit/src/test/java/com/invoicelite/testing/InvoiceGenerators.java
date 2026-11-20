package com.invoicelite.testing;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Random but valid inputs for property tests. All randomness comes from one seed per test run,
 * printed once, so any failure can be repeated with: ./gradlew test -PpropertySeed=<seed>
 */
public final class InvoiceGenerators {

    public static final Currency EUR = Currency.getInstance("EUR");
    /** How many generated samples each property checks. */
    public static final int SAMPLES = 25;
    public static final long SEED = chooseSeed();

    /** MUG-01 twice, so the same SKU often appears on several lines. */
    private static final List<String> SKUS = List.of("MUG-01", "MUG-01", "PEN-10", "CLIP-01");

    private final RandomGenerator random;

    private InvoiceGenerators(long seed) {
        this.random = new Random(seed);
    }

    /** A generator that starts from the run's seed, so every property gets a repeatable sequence. */
    public static InvoiceGenerators fromRunSeed() {
        return new InvoiceGenerators(SEED);
    }

    private static long chooseSeed() {
        String fixed = System.getProperty("invoicelite.seed");
        long seed = fixed != null ? Long.parseLong(fixed) : new Random().nextLong();
        System.out.println("Property samples use seed " + seed + " (repeat with -PpropertySeed=" + seed + ")");
        return seed;
    }

    /** An amount between -1,000,000.00 and 1,000,000.00 euros. */
    public Money euros() {
        long cents = random.nextLong(-100_000_000L, 100_000_001L);
        return new Money(BigDecimal.valueOf(cents, 2), EUR);
    }

    /** Any decimal number with up to 8 decimal places, to test how Money rounds. */
    public BigDecimal anyAmount() {
        return BigDecimal.valueOf(random.nextLong(-1_000_000_000_000L, 1_000_000_000_001L), random.nextInt(0, 9));
    }

    /** A line with a price from 0.01 to 999.99 and a quantity from 1 to 20. */
    public LineItem lineItem() {
        String sku = SKUS.get(random.nextInt(SKUS.size()));
        Money price = new Money(BigDecimal.valueOf(between(1, 99_999), 2), EUR);
        return new LineItem(sku, sku + " item", price, (int) between(1, 20));
    }

    /** Usually a random value, but often the smallest or the largest one: edges are where bugs like to hide. */
    private long between(long min, long max) {
        double pick = random.nextDouble();
        if (pick < 0.2) {
            return min;
        }
        if (pick < 0.3) {
            return max;
        }
        return random.nextLong(min, max + 1);
    }

    /** Between minSize and maxSize lines. */
    public List<LineItem> lines(int minSize, int maxSize) {
        int size = random.nextInt(minSize, maxSize + 1);
        List<LineItem> lines = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            lines.add(lineItem());
        }
        return lines;
    }

    public static Money subtotalOf(List<LineItem> lines) {
        return lines.stream().map(LineItem::total).reduce(Money.zero(EUR), Money::plus);
    }
}
