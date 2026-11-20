package com.invoicelite.numbering;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceNumberGeneratorTest {

    private final InvoiceSequence sequence = new InMemoryInvoiceSequence();

    @Test
    void numbersStartAtOneInTheYearOfTheClock() {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator(clockAt("2026-03-15T10:00:00Z"), sequence);

        assertThat(generator.next()).isEqualTo("INV-2026-0001");
        assertThat(generator.next()).isEqualTo("INV-2026-0002");
    }

    @Test
    void aNewYearStartsAgainAtOne() {
        new InvoiceNumberGenerator(clockAt("2026-12-30T10:00:00Z"), sequence).next();
        InvoiceNumberGenerator nextYear = new InvoiceNumberGenerator(clockAt("2027-01-02T10:00:00Z"), sequence);

        assertThat(nextYear.next()).isEqualTo("INV-2027-0001");
    }

    private static Clock clockAt(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
    }
}
