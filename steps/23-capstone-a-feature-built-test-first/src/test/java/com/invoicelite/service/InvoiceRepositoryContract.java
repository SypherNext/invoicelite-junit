package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import com.invoicelite.invoice.Payment;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.invoicelite.invoice.TestInvoices.EUR;
import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The rules every InvoiceRepository must follow. Each implementation's test class extends this one,
 * so the in-memory fake and the PostgreSQL repository are held to exactly the same tests.
 */
public abstract class InvoiceRepositoryContract {

    /** A new, empty repository for each test. */
    protected abstract InvoiceRepository repository();

    @Test
    void findsASavedInvoiceWithItsLines() {
        InvoiceRepository repository = repository();
        Invoice invoice = invoice("INV-2026-0001");
        invoice.addLine(line("MUG-01", "12.50", 4));
        invoice.addLine(line("PEN-10", "6.40", 2));

        repository.save(invoice);

        assertThat(repository.findByNumber("INV-2026-0001")).get()
                .usingRecursiveComparison()
                .isEqualTo(invoice);
    }

    @Test
    void savingAgainStoresTheNewStatusAndDates() {
        InvoiceRepository repository = repository();
        Invoice invoice = invoice("INV-2026-0001");
        invoice.addLine(line("MUG-01", "12.50", 1));
        repository.save(invoice);

        invoice.issue(LocalDate.of(2026, 3, 15), 30);
        repository.save(invoice);

        Invoice found = repository.findByNumber("INV-2026-0001").orElseThrow();
        assertThat(found.status()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(found.dueDate()).hasValue(LocalDate.of(2026, 4, 14));
    }

    @Test
    void aChangeIsStoredOnlyWhenSaved() {
        InvoiceRepository repository = repository();
        Invoice invoice = invoice("INV-2026-0001");
        invoice.addLine(line("MUG-01", "12.50", 1));
        repository.save(invoice);

        invoice.issue(LocalDate.of(2026, 3, 15), 30);

        assertThat(repository.findByNumber("INV-2026-0001")).get()
                .extracting(Invoice::status)
                .isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void keepsPaymentsInTheOrderTheyWereMade() {
        InvoiceRepository repository = repository();
        Invoice invoice = invoice("INV-2026-0001");
        invoice.addLine(line("MUG-01", "12.50", 4));
        invoice.issue(LocalDate.of(2026, 3, 15), 30);
        invoice.recordPayment(new Payment(Money.of("20.00", "EUR"), "ch_1", LocalDate.of(2026, 3, 20)), Money.of("59.50", "EUR"));
        invoice.recordPayment(new Payment(Money.of("39.50", "EUR"), "ch_2", LocalDate.of(2026, 4, 2)), Money.of("39.50", "EUR"));

        repository.save(invoice);

        Invoice found = repository.findByNumber("INV-2026-0001").orElseThrow();
        assertThat(found.payments()).containsExactlyElementsOf(invoice.payments());
        assertThat(found.status()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void findsInvoicesByStatus() {
        InvoiceRepository repository = repository();
        Invoice draft = invoice("INV-2026-0001");
        Invoice issued = invoice("INV-2026-0002");
        issued.addLine(line("MUG-01", "12.50", 1));
        issued.issue(LocalDate.of(2026, 3, 15), 30);
        repository.save(draft);
        repository.save(issued);

        assertThat(repository.findByStatus(InvoiceStatus.ISSUED))
                .extracting(Invoice::number)
                .containsExactly("INV-2026-0002");
        assertThat(repository.findByStatus(InvoiceStatus.PAID)).isEmpty();
    }

    @Test
    void anUnknownNumberIsEmpty() {
        assertThat(repository().findByNumber("INV-2026-0404")).isEmpty();
    }

    private static Invoice invoice(String number) {
        return new Invoice(number, "Acme GmbH", "DE", EUR);
    }
}
