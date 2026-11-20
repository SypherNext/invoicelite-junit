package com.invoicelite.service;

import com.invoicelite.money.Money;

/**
 * What the service asks the payment provider to charge. The idempotency key lets the provider
 * recognise a request that was sent twice, so the customer is charged only once.
 */
public record PaymentRequest(String invoiceNumber, Money amount, String idempotencyKey) {
}
