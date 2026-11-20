package com.invoicelite.invoice;

import com.invoicelite.money.Money;

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
    private final List<Payment> payments = new ArrayList<>();
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
                                  List<LineItem> lines, List<Payment> payments,
                                  InvoiceStatus status, LocalDate issueDate, LocalDate dueDate) {
        Invoice invoice = new Invoice(number, customer, countryCode, currency);
        lines.forEach(invoice::addLine);
        invoice.payments.addAll(payments);
        invoice.status = Objects.requireNonNull(status, "status");
        invoice.issueDate = issueDate;
        invoice.dueDate = dueDate;
        return invoice;
    }

    /** A separate object with the same content, so changes to one do not show in the other. */
    public Invoice copy() {
        return restore(number, customer, countryCode, currency, lines, payments, status, issueDate, dueDate);
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

    /**
     * Throws if this amount cannot be paid now: the invoice must be payable, and the amount
     * must be more than zero and no more than what is due.
     */
    public void checkPayment(Money amount, Money amountDue) {
        if (!isPayable()) {
            throw new IllegalStateException("Invoice " + number + " is " + status + " and cannot be paid");
        }
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("A payment must be more than zero, was " + amount.amount());
        }
        if (amount.amount().compareTo(amountDue.amount()) > 0) {
            throw new IllegalArgumentException("Payment of " + amount.amount() + " is more than the "
                    + amountDue.amount() + " due on " + number);
        }
    }

    /**
     * Records a payment. The invoice is paid when the payment covers everything that was due.
     */
    public void recordPayment(Payment payment, Money amountDue) {
        checkPayment(payment.amount(), amountDue);
        payments.add(payment);
        if (payment.amount().equals(amountDue)) {
            status = InvoiceStatus.PAID;
        }
    }

    public List<Payment> payments() {
        return List.copyOf(payments);
    }

    public Money amountPaid() {
        return payments.stream().map(Payment::amount).reduce(Money.zero(currency), Money::plus);
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
