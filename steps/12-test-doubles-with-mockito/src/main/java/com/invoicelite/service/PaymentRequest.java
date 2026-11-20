package com.invoicelite.service;

import com.invoicelite.money.Money;

/**
 * What the service asks the payment provider to charge.
 */
public record PaymentRequest(String invoiceNumber, Money amount) {
}
