package com.invoicelite.invoice;

import com.invoicelite.money.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Money received for an invoice: how much, the payment provider's reference, and the day it arrived.
 */
public record Payment(Money amount, String reference, LocalDate paidOn) {

    public Payment {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(paidOn, "paidOn");
    }
}
