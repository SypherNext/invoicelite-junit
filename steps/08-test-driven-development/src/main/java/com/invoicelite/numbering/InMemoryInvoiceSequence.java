package com.invoicelite.numbering;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps one counter per year in memory. Each year starts at 1.
 */
public final class InMemoryInvoiceSequence implements InvoiceSequence {

    private final Map<Integer, Integer> lastNumberByYear = new HashMap<>();

    @Override
    public int next(int year) {
        int next = lastNumberByYear.getOrDefault(year, 0) + 1;
        lastNumberByYear.put(year, next);
        return next;
    }
}
