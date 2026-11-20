package com.invoicelite.invoice;

import com.invoicelite.discount.DiscountPolicy;
import com.invoicelite.money.Money;
import com.invoicelite.tax.TaxCalculator;
import com.invoicelite.tax.TaxRounding;
import com.invoicelite.testing.InvoiceGenerators;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceCalculatorProperties {

    private final InvoiceCalculator calculator =
            new InvoiceCalculator(DiscountPolicy.none(), new TaxCalculator(TaxRounding.PER_INVOICE));

    static Stream<List<LineItem>> lineLists() {
        InvoiceGenerators generate = InvoiceGenerators.fromRunSeed();
        return IntStream.range(0, InvoiceGenerators.SAMPLES).mapToObj(i -> generate.lines(0, 8));
    }

    @ParameterizedTest(name = "sample {index}")
    @MethodSource("lineLists")
    void subtotalMatchesASimpleOracle(List<LineItem> lines) {
        Invoice invoice = TestInvoices.draftFor("DE");
        lines.forEach(invoice::addLine);

        // The oracle: the same sum done the obvious way, in plain BigDecimal arithmetic
        BigDecimal expected = BigDecimal.ZERO;
        for (LineItem line : lines) {
            expected = expected.add(line.unitPrice().amount().multiply(BigDecimal.valueOf(line.quantity())));
        }

        Money subtotal = calculator.subtotal(invoice);

        assertThat(subtotal.amount()).isEqualByComparingTo(expected);
    }
}
