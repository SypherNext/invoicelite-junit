package com.invoicelite.invoice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An invoice to one customer, in one currency.
 */
public final class Invoice {

    private final String number;
    private final String customer;
    private final String countryCode;
    private final Currency currency;
    private final List<LineItem> lines = new ArrayList<>();
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    private LocalDate issueDate;
    private LocalDate dueDate;

    public Invoice(String number, String customer, String countryCode, Currency currency) {
        this.number = Objects.requireNonNull(number, "number");
        this.customer = Objects.requireNonNull(customer, "customer");
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    /**
     * Rebuilds an invoice exactly as it was stored, for repositories. Normal code uses the constructor.
     */
    public static Invoice restore(String number, String customer, String countryCode, Currency currency,
                                  List<LineItem> lines, InvoiceStatus status, LocalDate issueDate, LocalDate dueDate) {
        Invoice invoice = new Invoice(number, customer, countryCode, currency);
        lines.forEach(invoice::addLine);
        invoice.status = Objects.requireNonNull(status, "status");
        invoice.issueDate = issueDate;
        invoice.dueDate = dueDate;
        return invoice;
    }

    /** A separate object with the same content, so changes to one do not show in the other. */
    public Invoice copy() {
        return restore(number, customer, countryCode, currency, lines, status, issueDate, dueDate);
    }

    public void addLine(LineItem line) {
        if (!line.unitPrice().currency().equals(currency)) {
            throw new IllegalArgumentException(
                    "Line " + line.sku() + " is in " + line.unitPrice().currency()
                            + " but invoice " + number + " is in " + currency);
        }
        lines.add(line);
    }

    /**
     * Issues the invoice on the given date. Payment is due paymentTermDays later.
     */
    public void issue(LocalDate issuedOn, int paymentTermDays) {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Invoice " + number + " is already " + status);
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("Invoice " + number + " has no lines");
        }
        this.issueDate = issuedOn;
        this.dueDate = issuedOn.plusDays(paymentTermDays);
        this.status = InvoiceStatus.ISSUED;
    }

    public boolean isOverdueOn(LocalDate date) {
        return status == InvoiceStatus.ISSUED && date.isAfter(dueDate);
    }

    public void markOverdue(LocalDate today) {
        if (!isOverdueOn(today)) {
            throw new IllegalStateException("Invoice " + number + " is not overdue on " + today);
        }
        status = InvoiceStatus.OVERDUE;
    }

    public boolean isPayable() {
        return status == InvoiceStatus.ISSUED || status == InvoiceStatus.OVERDUE;
    }

    public void markPaid() {
        if (!isPayable()) {
            throw new IllegalStateException("Invoice " + number + " is " + status + " and cannot be paid");
        }
        status = InvoiceStatus.PAID;
    }

    public Optional<LineItem> lineFor(String sku) {
        return lines.stream()
                .filter(line -> line.sku().equals(sku))
                .findFirst();
    }

    public List<LineItem> lines() {
        return List.copyOf(lines);
    }

    public String number() {
        return number;
    }

    public String customer() {
        return customer;
    }

    public String countryCode() {
        return countryCode;
    }

    public Currency currency() {
        return currency;
    }

    public InvoiceStatus status() {
        return status;
    }

    public Optional<LocalDate> issueDate() {
        return Optional.ofNullable(issueDate);
    }

    public Optional<LocalDate> dueDate() {
        return Optional.ofNullable(dueDate);
    }

    /** Two Invoice objects are the same invoice when they have the same number. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Invoice invoice && number.equals(invoice.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public String toString() {
        return "Invoice[" + number + ", " + status + "]";
    }
}
