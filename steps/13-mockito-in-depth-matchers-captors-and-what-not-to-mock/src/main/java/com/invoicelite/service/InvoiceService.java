package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceCalculator;
import com.invoicelite.invoice.InvoiceStatus;
import com.invoicelite.invoice.LineItem;
import com.invoicelite.numbering.InvoiceNumberGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Creates, issues and collects invoices. Every outside dependency comes in through the constructor.
 */
public final class InvoiceService {

    static final int PAYMENT_TERM_DAYS = 30;

    private final InvoiceRepository repository;
    private final InvoiceNumberGenerator numbers;
    private final InvoiceCalculator calculator;
    private final PaymentGateway gateway;
    private final Notifier notifier;
    private final Clock clock;

    public InvoiceService(InvoiceRepository repository, InvoiceNumberGenerator numbers,
                          InvoiceCalculator calculator, PaymentGateway gateway, Notifier notifier, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.numbers = Objects.requireNonNull(numbers, "numbers");
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.notifier = Objects.requireNonNull(notifier, "notifier");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Invoice createDraft(String customer, String countryCode, Currency currency, List<LineItem> lines) {
        Invoice invoice = new Invoice(numbers.next(), customer, countryCode, currency);
        lines.forEach(invoice::addLine);
        repository.save(invoice);
        return invoice;
    }

    public Invoice issue(String number) {
        Invoice invoice = find(number);
        invoice.issue(today(), PAYMENT_TERM_DAYS);
        repository.save(invoice);
        notifier.invoiceIssued(invoice);
        return invoice;
    }

    public PaymentResult pay(String number) {
        Invoice invoice = find(number);
        if (!invoice.isPayable()) {
            throw new IllegalStateException("Invoice " + number + " is " + invoice.status() + " and cannot be paid");
        }
        PaymentResult result = gateway.charge(new PaymentRequest(number, calculator.totals(invoice).total()));
        if (!result.approved()) {
            throw new PaymentDeclinedException(number, result.declineReason());
        }
        invoice.markPaid();
        repository.save(invoice);
        notifier.paymentReceived(invoice, result.reference());
        return result;
    }

    public int markOverdueInvoices() {
        LocalDate today = today();
        List<Invoice> overdue = repository.findByStatus(InvoiceStatus.ISSUED).stream()
                .filter(invoice -> invoice.isOverdueOn(today))
                .toList();
        for (Invoice invoice : overdue) {
            invoice.markOverdue(today);
            repository.save(invoice);
            notifier.invoiceOverdue(invoice);
        }
        return overdue.size();
    }

    private Invoice find(String number) {
        return repository.findByNumber(number).orElseThrow(() -> new InvoiceNotFoundException(number));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
