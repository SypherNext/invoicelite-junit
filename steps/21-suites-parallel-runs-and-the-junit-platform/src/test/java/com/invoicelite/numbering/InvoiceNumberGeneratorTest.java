package com.invoicelite.numbering;

import com.invoicelite.testing.FixedClock;
import com.invoicelite.testing.SlowTestReporter;
import com.invoicelite.testing.WithLocale;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceNumberGeneratorTest {

    @RegisterExtension
    static final SlowTestReporter SLOW_TESTS = new SlowTestReporter(Duration.ofMillis(20));

    private final InvoiceSequence sequence = new InMemoryInvoiceSequence();

    @Test
    @FixedClock("2026-03-15T10:00:00Z")
    void numbersStartAtOneInTheYearOfTheClock(Clock clock) {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator(clock, sequence);

        assertThat(generator.next()).isEqualTo("INV-2026-0001");
        assertThat(generator.next()).isEqualTo("INV-2026-0002");
    }

    @Test
    @WithLocale("ar-EG")
    @FixedClock("2026-03-15T10:00:00Z")
    void numbersUseLatinDigitsWhateverTheDefaultLocale(Clock clock) {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator(clock, sequence);

        assertThat(generator.next()).isEqualTo("INV-2026-0001");
    }

    @Test
    void aNewYearStartsAgainAtOne() {
        new InvoiceNumberGenerator(clockAt("2026-12-30T10:00:00Z", "UTC"), sequence).next();
        InvoiceNumberGenerator nextYear = new InvoiceNumberGenerator(clockAt("2027-01-02T10:00:00Z", "UTC"), sequence);

        assertThat(nextYear.next()).isEqualTo("INV-2027-0001");
    }

    @Test
    void usesTheYearInTheClocksTimeZone() {
        // 23:30 in London (UTC) on New Year's Eve is already 00:30 on 1 January in Berlin
        String inLondon = new InvoiceNumberGenerator(clockAt("2026-12-31T23:30:00Z", "UTC"), sequence).next();
        String inBerlin = new InvoiceNumberGenerator(clockAt("2026-12-31T23:30:00Z", "Europe/Berlin"), sequence).next();

        assertThat(inLondon).isEqualTo("INV-2026-0001");
        assertThat(inBerlin).isEqualTo("INV-2027-0001");
    }

    @Test
    @Tag("slow")
    @FixedClock("2026-06-01T10:00:00Z")
    void numbersStayUniqueForAYearOfInvoices(Clock clock) {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator(clock, sequence);
        Set<String> numbers = new HashSet<>();

        for (int i = 0; i < 9_999; i++) {
            numbers.add(generator.next());
        }

        assertThat(numbers).hasSize(9_999).contains("INV-2026-9999");
    }

    private static Clock clockAt(String instant, String zone) {
        return Clock.fixed(Instant.parse(instant), ZoneId.of(zone));
    }
}
