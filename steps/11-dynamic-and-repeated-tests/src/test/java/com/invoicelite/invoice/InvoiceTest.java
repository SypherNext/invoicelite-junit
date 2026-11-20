package com.invoicelite.invoice;

import com.invoicelite.money.Money;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("An invoice")
class InvoiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private final Invoice invoice = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);
    private final LineItem mug = new LineItem("MUG-01", "Enamel mug", Money.of("12.50", "EUR"), 2);
    private final LineItem pen = new LineItem("PEN-10", "Gel pen pack", Money.of("6.40", "EUR"), 1);

    @Test
    @DisplayName("starts with no lines")
    void startsEmpty() {
        assertThat(invoice.lines()).isEmpty();
    }

    @Test
    @DisplayName("keeps lines in the order they were added")
    void keepsLineOrder() {
        invoice.addLine(mug);
        invoice.addLine(pen);

        assertThat(invoice.lines())
                .extracting(LineItem::sku)
                .containsExactly("MUG-01", "PEN-10");
    }

    @Test
    @DisplayName("finds a line by SKU")
    void findsLineBySku() {
        invoice.addLine(mug);
        invoice.addLine(pen);

        assertThat(invoice.lineFor("PEN-10")).hasValue(pen);
        assertThat(invoice.lineFor("CUP-99")).isEmpty();
    }

    @Test
    @DisplayName("stores every field of a line")
    void storesEveryField() {
        invoice.addLine(mug);

        LineItem stored = invoice.lines().getFirst();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(stored.sku()).isEqualTo("MUG-01");
            softly.assertThat(stored.description()).isEqualTo("Enamel mug");
            softly.assertThat(stored.unitPrice()).isEqualTo(Money.of("12.50", "EUR"));
            softly.assertThat(stored.quantity()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("refuses a line in another currency")
    void refusesOtherCurrency() {
        LineItem dollarLine = new LineItem("MUG-01", "Enamel mug", Money.of("12.50", "USD"), 1);

        assertThatThrownBy(() -> invoice.addLine(dollarLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("USD")
                .hasMessageContaining("INV-2026-0001");
        assertThat(invoice.lines()).as("lines after a refused add").isEmpty();
    }

    @Test
    @DisplayName("cannot be issued without lines")
    void cannotIssueEmptyInvoice() {
        assertThatThrownBy(() -> invoice.issue(LocalDate.of(2026, 3, 15), 30))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice INV-2026-0001 has no lines");
    }

    @Test
    @DisplayName("does not let callers change its lines")
    void linesAreReadOnly() {
        invoice.addLine(mug);

        assertThatThrownBy(() -> invoice.lines().add(pen))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
