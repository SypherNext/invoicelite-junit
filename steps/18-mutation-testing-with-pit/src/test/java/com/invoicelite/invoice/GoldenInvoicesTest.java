package com.invoicelite.invoice;

import com.invoicelite.discount.PercentageDiscount;
import com.invoicelite.money.Money;
import com.invoicelite.tax.TaxCalculator;
import com.invoicelite.tax.TaxRounding;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Checks the calculator against invoices worked out by hand, one test per row of golden-invoices.csv.
 */
class GoldenInvoicesTest {

    @TestFactory
    Stream<DynamicNode> totalsMatchTheGoldenFile() throws IOException {
        Map<String, List<GoldenInvoice>> byCountry = readGoldenFile().stream()
                .collect(groupingBy(GoldenInvoice::countryCode, TreeMap::new, toList()));

        return byCountry.entrySet().stream()
                .map(country -> dynamicContainer(country.getKey(), country.getValue().stream()
                        .map(golden -> dynamicTest(golden.name(),
                                () -> assertThat(golden.calculate()).isEqualTo(golden.expected())))));
    }

    private static List<GoldenInvoice> readGoldenFile() throws IOException {
        try (InputStream in = GoldenInvoicesTest.class.getResourceAsStream("/golden-invoices.csv")) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return text.lines()
                    .filter(row -> !row.isBlank() && !row.startsWith("#"))
                    .map(GoldenInvoice::parse)
                    .toList();
        }
    }

    record GoldenInvoice(String name, String countryCode, TaxRounding rounding, List<LineItem> lines,
                         BigDecimal discountPercent, InvoiceTotals expected) {

        static GoldenInvoice parse(String row) {
            String[] cells = row.split(";");
            List<LineItem> lines = Stream.of(cells[3].split(", ")).map(GoldenInvoice::parseLine).toList();
            InvoiceTotals expected = new InvoiceTotals(
                    euros(cells[5]), euros(cells[6]), euros(cells[7]), euros(cells[8]));
            return new GoldenInvoice(cells[0], cells[1], TaxRounding.valueOf(cells[2]), lines,
                    new BigDecimal(cells[4]), expected);
        }

        private static LineItem parseLine(String text) {
            String[] parts = text.split(" ");
            int quantity = Integer.parseInt(parts[2].substring(1));
            return new LineItem(parts[0], parts[0], euros(parts[1]), quantity);
        }

        private static Money euros(String amount) {
            return Money.of(amount, "EUR");
        }

        InvoiceTotals calculate() {
            Invoice invoice = TestInvoices.draftFor(countryCode);
            lines.forEach(invoice::addLine);
            InvoiceCalculator calculator = new InvoiceCalculator(
                    new PercentageDiscount(discountPercent), new TaxCalculator(rounding));
            return calculator.totals(invoice);
        }
    }
}
