package com.invoicelite.tax;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.LineItem;
import com.invoicelite.invoice.TestInvoices;
import com.invoicelite.money.Money;
import com.invoicelite.testing.InvoiceGenerators;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TaxProperties {

    static Stream<List<LineItem>> lineLists() {
        InvoiceGenerators generate = InvoiceGenerators.fromRunSeed();
        return IntStream.range(0, InvoiceGenerators.SAMPLES).mapToObj(i -> generate.lines(0, 8));
    }

    @ParameterizedTest(name = "sample {index}")
    @MethodSource("lineLists")
    void perLineAndPerInvoiceRoundingDifferByAtMostOneCentPerLine(List<LineItem> lines) {
        Invoice invoice = TestInvoices.draftFor("DE");
        lines.forEach(invoice::addLine);

        Money perLine = new TaxCalculator(TaxRounding.PER_LINE).taxFor(invoice);
        Money perInvoice = new TaxCalculator(TaxRounding.PER_INVOICE).taxFor(invoice);

        BigDecimal difference = perLine.minus(perInvoice).amount().abs();
        assertThat(difference).isLessThanOrEqualTo(new BigDecimal("0.01").multiply(BigDecimal.valueOf(lines.size())));
    }
}
