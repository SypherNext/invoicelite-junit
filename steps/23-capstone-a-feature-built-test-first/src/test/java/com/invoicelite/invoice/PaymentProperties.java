package com.invoicelite.invoice;

import com.invoicelite.discount.DiscountPolicy;
import com.invoicelite.money.Money;
import com.invoicelite.tax.TaxCalculator;
import com.invoicelite.tax.TaxRounding;
import com.invoicelite.testing.InvoiceGenerators;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentProperties {

    private final InvoiceCalculator calculator =
            new InvoiceCalculator(DiscountPolicy.none(), new TaxCalculator(TaxRounding.PER_INVOICE));

    static Stream<Arguments> invoicesAndPaymentAttempts() {
        InvoiceGenerators generate = InvoiceGenerators.fromRunSeed();
        return IntStream.range(0, InvoiceGenerators.SAMPLES)
                .mapToObj(i -> Arguments.of(generate.lines(1, 5), generate.paymentAmounts(6)));
    }

    @ParameterizedTest(name = "sample {index}")
    @MethodSource("invoicesAndPaymentAttempts")
    void paymentsNeverExceedTheTotalAndPaidMeansNothingIsDue(List<LineItem> lines, List<BigDecimal> attempts) {
        Invoice invoice = TestInvoices.draftFor("DE");
        lines.forEach(invoice::addLine);
        invoice.issue(LocalDate.of(2026, 3, 15), 30);
        Money total = calculator.totals(invoice).total();

        for (BigDecimal attempt : attempts) {
            Money due = calculator.amountDue(invoice);
            Money amount = new Money(attempt, InvoiceGenerators.EUR);
            if (invoice.isPayable() && attempt.compareTo(due.amount()) <= 0) {
                invoice.recordPayment(new Payment(amount, "ch_test", LocalDate.of(2026, 3, 20)), due);
            }
        }

        assertThat(invoice.amountPaid().amount()).isLessThanOrEqualTo(total.amount());
        boolean nothingDue = calculator.amountDue(invoice).amount().signum() == 0;
        assertThat(invoice.status() == InvoiceStatus.PAID).isEqualTo(nothingDue);
    }
}
