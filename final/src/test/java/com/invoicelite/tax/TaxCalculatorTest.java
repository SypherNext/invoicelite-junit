package com.invoicelite.tax;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest(name = "{0} charges {1}")
    @CsvSource({
            "DE, 0.19",
            "FR, 0.20",
            "NL, 0.21",
            "IE, 0.23"
    })
    void knowsTheRateOfEachCountry(String countryCode, String expectedRate) {
        assertThat(perLine.rateFor(countryCode)).isEqualByComparingTo(expectedRate);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"US", "GB", "de"})
    void refusesACountryWithoutARate(String countryCode) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> perLine.rateFor(countryCode))
                .withMessage("No tax rate for country " + countryCode);
    }

    @ParameterizedTest
    @EnumSource(TaxRounding.class)
    void taxesATypicalInvoiceTheSameWayInBothModes(TaxRounding rounding) {
        Invoice invoice = draftFor("DE");
        invoice.addLine(line("MUG-01", "12.50", 4));
        invoice.addLine(line("PEN-10", "6.40", 2));

        assertThat(new TaxCalculator(rounding).taxFor(invoice)).isEqualTo(Money.of("11.93", "EUR"));
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
