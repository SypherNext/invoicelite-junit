package com.invoicelite.service;

public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String invoiceNumber, String reason) {
        super("Payment for " + invoiceNumber + " was declined: " + reason);
    }
}
