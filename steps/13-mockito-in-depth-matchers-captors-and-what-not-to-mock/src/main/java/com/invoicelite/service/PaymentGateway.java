package com.invoicelite.service;

/**
 * Charges a customer through an outside payment provider.
 */
public interface PaymentGateway {

    PaymentResult charge(PaymentRequest request);
}
