package com.invoicelite.invoice;

import com.invoicelite.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static com.invoicelite.invoice.TestInvoices.draftFor;
import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@DisplayName("Paying an invoice in parts")
class InvoicePaymentsTest {

    private static final LocalDate MARCH_20 = LocalDate.of(2026, 3, 20);
    private static final Money DUE = Money.of("74.73", "EUR");

    private final Invoice invoice = draftFor("DE");

    @BeforeEach
    void addLines() {
        invoice.addLine(line("MUG-01", "12.50", 4));
        invoice.addLine(line("PEN-10", "6.40", 2));
    }

    @Test
    @DisplayName("a draft cannot take a payment")
    void draftRefusesPayment() {
        assertThatIllegalStateException()
                .isThrownBy(() -> invoice.recordPayment(payment("30.00"), DUE))
                .withMessage("Invoice INV-2026-0001 is DRAFT and cannot be paid");
    }

    @Nested
    @DisplayName("once issued")
    class Issued {

        @BeforeEach
        void issue() {
            invoice.issue(LocalDate.of(2026, 3, 15), 30);
        }

        @Test
        @DisplayName("a part payment is recorded and the invoice stays open")
        void partPaymentKeepsItOpen() {
            invoice.recordPayment(payment("30.00"), DUE);

            assertThat(invoice.payments()).containsExactly(payment("30.00"));
            assertThat(invoice.amountPaid()).isEqualTo(Money.of("30.00", "EUR"));
            assertThat(invoice.status()).isEqualTo(InvoiceStatus.ISSUED);
        }

        @Test
        @DisplayName("the payment that clears the amount due marks it paid")
        void lastPaymentMarksItPaid() {
            invoice.recordPayment(payment("30.00"), DUE);

            invoice.recordPayment(payment("44.73"), Money.of("44.73", "EUR"));

            assertThat(invoice.amountPaid()).isEqualTo(DUE);
            assertThat(invoice.status()).isEqualTo(InvoiceStatus.PAID);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-5.00"})
        @DisplayName("refuses a payment that is not more than zero")
        void refusesZeroOrLess(String amount) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> invoice.recordPayment(payment(amount), DUE))
                    .withMessage("A payment must be more than zero, was " + amount);
            assertThat(invoice.payments()).isEmpty();
        }

        @Test
        @DisplayName("refuses a payment of more than the amount due")
        void refusesOverpayment() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> invoice.recordPayment(payment("80.00"), DUE))
                    .withMessage("Payment of 80.00 is more than the 74.73 due on INV-2026-0001");
        }
    }

    private static Payment payment(String amount) {
        return new Payment(Money.of(amount, "EUR"), "ch_" + amount, MARCH_20);
    }
}
