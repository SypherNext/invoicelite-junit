package com.invoicelite.invoice;

import com.invoicelite.discount.DiscountPolicy;
import com.invoicelite.discount.PercentageDiscount;
import com.invoicelite.money.Money;
import com.invoicelite.tax.TaxCalculator;
import com.invoicelite.tax.TaxRounding;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.invoicelite.invoice.TestInvoices.draftFor;
import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceCalculatorTest {

    private final TaxCalculator tax = new TaxCalculator(TaxRounding.PER_INVOICE);

    @Test
    void subtotal_of_an_invoice_without_lines_is_zero() {
        InvoiceCalculator calculator = new InvoiceCalculator(DiscountPolicy.none(), tax);

        Money subtotal = calculator.subtotal(draftFor("DE"));

        assertThat(subtotal).isEqualTo(Money.of("0.00", "EUR"));
    }

    @Test
    void subtotal_adds_up_every_line_total() {
        InvoiceCalculator calculator = new InvoiceCalculator(DiscountPolicy.none(), tax);

        Money subtotal = calculator.subtotal(mugsAndPens());

        assertThat(subtotal).isEqualTo(Money.of("62.80", "EUR"));
    }

    @Test
    void totals_take_the_discount_off_before_tax() {
        InvoiceCalculator calculator = new InvoiceCalculator(new PercentageDiscount(new BigDecimal("10")), tax);

        InvoiceTotals totals = calculator.totals(mugsAndPens());

        assertThat(totals).isEqualTo(new InvoiceTotals(
                Money.of("62.80", "EUR"),
                Money.of("6.28", "EUR"),
                Money.of("10.74", "EUR"),
                Money.of("67.26", "EUR")));
    }

    @Test
    void amount_due_is_the_total_less_what_was_paid() {
        InvoiceCalculator calculator = new InvoiceCalculator(DiscountPolicy.none(), tax);
        Invoice invoice = mugsAndPens();
        invoice.issue(LocalDate.of(2026, 3, 15), 30);
        Money total = calculator.totals(invoice).total();
        invoice.recordPayment(new Payment(Money.of("30.00", "EUR"), "ch_1", LocalDate.of(2026, 3, 20)), total);

        assertThat(calculator.amountDue(invoice)).isEqualTo(Money.of("44.73", "EUR"));
    }

    private static Invoice mugsAndPens() {
        Invoice invoice = draftFor("DE");
        invoice.addLine(line("MUG-01", "12.50", 4));
        invoice.addLine(line("PEN-10", "6.40", 2));
        return invoice;
    }
}
