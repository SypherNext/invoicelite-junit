package com.invoicelite.numbering;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Creates invoice numbers such as INV-2026-0001: the year of issue, then a running number.
 * Formatting uses Locale.ROOT, so the digits are 0 to 9 whatever the server's default locale is.
 */
public final class InvoiceNumberGenerator {

    private final Clock clock;
    private final InvoiceSequence sequence;

    public InvoiceNumberGenerator(Clock clock, InvoiceSequence sequence) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.sequence = Objects.requireNonNull(sequence, "sequence");
    }

    public String next() {
        int year = LocalDate.now(clock).getYear();
        return String.format(Locale.ROOT, "INV-%d-%04d", year, sequence.next(year));
    }
}
