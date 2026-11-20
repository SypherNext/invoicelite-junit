package com.invoicelite.service;

import com.invoicelite.discount.DiscountPolicy;
import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceCalculator;
import com.invoicelite.numbering.InMemoryInvoiceSequence;
import com.invoicelite.numbering.InvoiceNumberGenerator;
import com.invoicelite.tax.TaxCalculator;
import com.invoicelite.tax.TaxRounding;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import static com.invoicelite.invoice.TestInvoices.EUR;
import static com.invoicelite.invoice.TestInvoices.line;

/**
 * Builds an InvoiceService with real collaborators except the ones a test replaces.
 */
final class InvoiceServiceFixture {

    static final Clock MARCH_15 = Clock.fixed(Instant.parse("2026-03-15T09:00:00Z"), ZoneId.of("Europe/Berlin"));
    static final long SEED = 42;

    private InvoiceServiceFixture() {
    }

    static InvoiceService service(InvoiceRepository repository, PaymentGateway gateway, Notifier notifier, Clock clock) {
        InvoiceNumberGenerator numbers = new InvoiceNumberGenerator(clock, new InMemoryInvoiceSequence());
        InvoiceCalculator calculator = new InvoiceCalculator(DiscountPolicy.none(), new TaxCalculator(TaxRounding.PER_INVOICE));
        return new InvoiceService(repository, numbers, calculator, gateway, notifier, keys(), clock);
    }

    /** The idempotency keys the service will use, from the same seed: the first is idem-ba419d350dfe8af7. */
    static IdempotencyKeys keys() {
        return new IdempotencyKeys(new Random(SEED));
    }

    /** A draft for Acme GmbH in Germany: 4 mugs at 12.50 and 2 pen packs at 6.40, total 74.73 with tax. */
    static Invoice createMugsAndPens(InvoiceService service) {
        return service.createDraft("Acme GmbH", "DE", EUR,
                List.of(line("MUG-01", "12.50", 4), line("PEN-10", "6.40", 2)));
    }
}
