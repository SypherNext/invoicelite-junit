package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;

/**
 * Tells the customer what happened to an invoice (by email, in the real system).
 */
public interface Notifier {

    void invoiceIssued(Invoice invoice);

    void paymentReceived(Invoice invoice, String paymentReference);

    void invoiceOverdue(Invoice invoice);
}
