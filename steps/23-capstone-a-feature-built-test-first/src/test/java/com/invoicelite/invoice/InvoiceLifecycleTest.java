package com.invoicelite.invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.invoicelite.invoice.TestInvoices.draftFor;
import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@DisplayName("Invoice lifecycle")
class InvoiceLifecycleTest {

    private static final LocalDate MARCH_15 = LocalDate.of(2026, 3, 15);
    private static final LocalDate APRIL_14 = LocalDate.of(2026, 4, 14);
    private static final LocalDate APRIL_15 = LocalDate.of(2026, 4, 15);

    private final Invoice invoice = draftFor("DE");

    @BeforeEach
    void addALine() {
        invoice.addLine(line("MUG-01", "12.50", 2));
    }

    @Nested
    @DisplayName("a draft")
    class Draft {

        @Test
        @DisplayName("can be issued")
        void canBeIssued() {
            invoice.issue(MARCH_15, 30);

            assertThat(invoice.status()).isEqualTo(InvoiceStatus.ISSUED);
        }

        @Test
        @DisplayName("cannot be paid")
        void cannotBePaid() {
            assertThatIllegalStateException()
                    .isThrownBy(invoice::markPaid)
                    .withMessage("Invoice INV-2026-0001 is DRAFT and cannot be paid");
        }

        @Test
        @DisplayName("is never overdue")
        void isNeverOverdue() {
            assertThat(invoice.isOverdueOn(LocalDate.of(2030, 1, 1))).isFalse();
        }
    }

    @Nested
    @DisplayName("an issued invoice")
    class Issued {

        @BeforeEach
        void issue() {
            invoice.issue(MARCH_15, 30);
        }

        @Test
        @DisplayName("is due 30 days after it is issued")
        void hasDates() {
            assertThat(invoice.issueDate()).hasValue(MARCH_15);
            assertThat(invoice.dueDate()).hasValue(APRIL_14);
        }

        @Test
        @DisplayName("cannot be issued again")
        void cannotBeIssuedAgain() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> invoice.issue(MARCH_15, 30))
                    .withMessage("Invoice INV-2026-0001 is already ISSUED");
        }

        @Test
        @DisplayName("is overdue from the day after its due date")
        void overdueAfterDueDate() {
            assertThat(invoice.isOverdueOn(APRIL_14)).isFalse();
            assertThat(invoice.isOverdueOn(APRIL_15)).isTrue();
        }

        @Test
        @DisplayName("cannot be marked overdue on its due date")
        void notOverdueOnDueDate() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> invoice.markOverdue(APRIL_14))
                    .withMessage("Invoice INV-2026-0001 is not overdue on 2026-04-14");
        }

        @Nested
        @DisplayName("that is overdue")
        class Overdue {

            @BeforeEach
            void markOverdue() {
                invoice.markOverdue(APRIL_15);
            }

            @Test
            @DisplayName("has status OVERDUE")
            void hasStatusOverdue() {
                assertThat(invoice.status()).isEqualTo(InvoiceStatus.OVERDUE);
            }

            @Test
            @DisplayName("can still be paid")
            void canStillBePaid() {
                invoice.markPaid();

                assertThat(invoice.status()).isEqualTo(InvoiceStatus.PAID);
            }
        }
    }

    @Nested
    @DisplayName("a paid invoice")
    class Paid {

        @BeforeEach
        void issueAndPay() {
            invoice.issue(MARCH_15, 30);
            invoice.markPaid();
        }

        @Test
        @DisplayName("cannot be paid twice")
        void cannotBePaidTwice() {
            assertThatIllegalStateException()
                    .isThrownBy(invoice::markPaid)
                    .withMessage("Invoice INV-2026-0001 is PAID and cannot be paid");
        }

        @Test
        @DisplayName("never becomes overdue")
        void neverOverdue() {
            assertThat(invoice.isOverdueOn(LocalDate.of(2027, 1, 1))).isFalse();
        }
    }
}
