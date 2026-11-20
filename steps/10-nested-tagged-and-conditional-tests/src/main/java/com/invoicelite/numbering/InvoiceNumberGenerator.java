package com.invoicelite.numbering;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Creates invoice numbers such as INV-2026-0001: the year of issue, then a running number.
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
        return "INV-%d-%04d".formatted(year, sequence.next(year));
    }
}
