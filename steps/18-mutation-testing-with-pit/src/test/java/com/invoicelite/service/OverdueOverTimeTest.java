package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.testing.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static com.invoicelite.service.InvoiceServiceFixture.createMugsAndPens;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OverdueOverTimeTest {

    @Mock
    private PaymentGateway gateway;

    @Mock
    private Notifier notifier;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-03-15T09:00:00Z"), ZoneId.of("Europe/Berlin"));
    private InvoiceService service;

    @BeforeEach
    void createService() {
        service = InvoiceServiceFixture.service(new InMemoryInvoiceRepository(), gateway, notifier, clock);
    }

    @Test
    void anInvoiceTurnsOverdueOnDayThirtyOne() {
        Invoice invoice = createMugsAndPens(service);
        service.issue(invoice.number());

        clock.advance(Duration.ofDays(30));
        assertThat(service.markOverdueInvoices()).as("on the due date").isZero();

        clock.advance(Duration.ofDays(1));
        assertThat(service.markOverdueInvoices()).as("one day later").isEqualTo(1);
    }
}
