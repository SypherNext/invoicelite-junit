package com.invoicelite.tax;

/**
 * Where tax is rounded to cents.
 */
public enum TaxRounding {
    /** Round the tax of every line, then add the rounded amounts. */
    PER_LINE,
    /** Add the line totals first, then round the tax once. */
    PER_INVOICE
}
