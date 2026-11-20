package com.invoicelite.numbering;

/**
 * Hands out the running number of invoices within one year.
 */
public interface InvoiceSequence {

    int next(int year);
}
