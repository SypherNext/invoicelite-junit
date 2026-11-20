package com.invoicelite.tax;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.invoicelite.invoice.TestInvoices.draftFor;
import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TaxCalculatorTest {

    private TaxCalculator perLine;
    private TaxCalculator perInvoice;
    private Invoice threeSmallLines;

    @BeforeEach
    void setUp() {
        perLine = new TaxCalculator(TaxRounding.PER_LINE);
        perInvoice = new TaxCalculator(TaxRounding.PER_INVOICE);
        threeSmallLines = draftFor("DE");
        threeSmallLines.addLine(line("CLIP-01", "0.25", 1));
        threeSmallLines.addLine(line("CLIP-02", "0.25", 1));
        threeSmallLines.addLine(line("CLIP-03", "0.25", 1));
    }

    @Test
    void knowsTheGermanRate() {
        assertThat(perLine.rateFor("DE")).isEqualByComparingTo("0.19");
    }

    @Test
    void refusesACountryWithoutARate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> perLine.rateFor("US"))
                .withMessage("No tax rate for country US");
    }

    @Test
    void taxesATypicalInvoice() {
        Invoice invoice = draftFor("DE");
        invoice.addLine(line("MUG-01", "12.50", 4));
        invoice.addLine(line("PEN-10", "6.40", 2));

        assertThat(perInvoice.taxFor(invoice)).isEqualTo(Money.of("11.93", "EUR"));
    }

    @Test
    void roundsEveryLineWhenRoundingPerLine() {
        // 0.25 x 0.19 = 0.0475, rounded to 0.05, three times
        assertThat(perLine.taxFor(threeSmallLines)).isEqualTo(Money.of("0.15", "EUR"));
    }

    @Test
    void roundsOnceWhenRoundingPerInvoice() {
        // 0.75 x 0.19 = 0.1425, rounded once to 0.14
        assertThat(perInvoice.taxFor(threeSmallLines)).isEqualTo(Money.of("0.14", "EUR"));
    }
}
