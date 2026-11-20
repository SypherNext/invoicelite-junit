package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.invoicelite.invoice.TestInvoices.EUR;
import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;

class InMemoryInvoiceRepositoryTest {

    private final InMemoryInvoiceRepository repository = new InMemoryInvoiceRepository();

    @Test
    void findsInvoicesByStatus() {
        Invoice draft = new Invoice("INV-2026-0001", "Acme GmbH", "DE", EUR);
        Invoice issued = new Invoice("INV-2026-0002", "Brightside BV", "NL", EUR);
        issued.addLine(line("MUG-01", "12.50", 1));
        issued.issue(LocalDate.of(2026, 3, 15), 30);
        repository.save(draft);
        repository.save(issued);

        assertThat(repository.findByStatus(InvoiceStatus.ISSUED)).containsExactly(issued);
        assertThat(repository.findByStatus(InvoiceStatus.PAID)).isEmpty();
    }

    @Test
    void anUnknownNumberIsEmpty() {
        assertThat(repository.findByNumber("INV-2026-0404")).isEmpty();
    }
}
