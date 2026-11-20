package com.invoicelite.service;

/**
 * The payment provider could not be reached or gave an answer that makes no sense.
 * The customer was not charged, so the invoice stays unpaid.
 */
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
