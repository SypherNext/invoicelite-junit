package com.invoicelite.numbering;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

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

    @Test
    void usesTheYearInTheClocksTimeZone() {
        // 23:30 in London (UTC) on New Year's Eve is already 00:30 on 1 January in Berlin
        Instant newYearsEve = Instant.parse("2026-12-31T23:30:00Z");

        String inLondon = new InvoiceNumberGenerator(Clock.fixed(newYearsEve, ZoneOffset.UTC), sequence).next();
        String inBerlin = new InvoiceNumberGenerator(Clock.fixed(newYearsEve, ZoneId.of("Europe/Berlin")), sequence).next();

        assertThat(inLondon).isEqualTo("INV-2026-0001");
        assertThat(inBerlin).isEqualTo("INV-2027-0001");
    }

    @Test
    @Tag("slow")
    void numbersStayUniqueForAYearOfInvoices() {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator(clockAt("2026-06-01T10:00:00Z"), sequence);
        Set<String> numbers = new HashSet<>();

        for (int i = 0; i < 9_999; i++) {
            numbers.add(generator.next());
        }

        assertThat(numbers).hasSize(9_999).contains("INV-2026-9999");
    }

    private static Clock clockAt(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
    }
}
