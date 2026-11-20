package com.invoicelite.numbering;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps one counter per year in memory. Each year starts at 1. Safe to use from many threads.
 */
public final class InMemoryInvoiceSequence implements InvoiceSequence {

    private final Map<Integer, Integer> lastNumberByYear = new ConcurrentHashMap<>();

    @Override
    public int next(int year) {
        return lastNumberByYear.merge(year, 1, Integer::sum);
    }
}
