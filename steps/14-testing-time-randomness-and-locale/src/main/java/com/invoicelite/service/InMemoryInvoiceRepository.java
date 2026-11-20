package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps invoices in a map. Used in tests and for trying the service without a database.
 */
public final class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<String, Invoice> invoicesByNumber = new ConcurrentHashMap<>();

    @Override
    public void save(Invoice invoice) {
        invoicesByNumber.put(invoice.number(), invoice);
    }

    @Override
    public Optional<Invoice> findByNumber(String number) {
        return Optional.ofNullable(invoicesByNumber.get(number));
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return invoicesByNumber.values().stream()
                .filter(invoice -> invoice.status() == status)
                .toList();
    }
}
