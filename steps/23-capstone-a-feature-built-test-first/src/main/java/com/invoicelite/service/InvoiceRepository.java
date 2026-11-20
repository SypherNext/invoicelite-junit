package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;

import java.util.List;
import java.util.Optional;

/**
 * Stores invoices. The service only knows this interface, not where the invoices live.
 */
public interface InvoiceRepository {

    void save(Invoice invoice);

    Optional<Invoice> findByNumber(String number);

    List<Invoice> findByStatus(InvoiceStatus status);
}
