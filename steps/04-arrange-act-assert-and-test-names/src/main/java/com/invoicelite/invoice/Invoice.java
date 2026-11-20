package com.invoicelite.invoice;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * An invoice to one customer, in one currency.
 */
public final class Invoice {

    private final String number;
    private final String customer;
    private final String countryCode;
    private final Currency currency;
    private final List<LineItem> lines = new ArrayList<>();

    public Invoice(String number, String customer, String countryCode, Currency currency) {
        this.number = Objects.requireNonNull(number, "number");
        this.customer = Objects.requireNonNull(customer, "customer");
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public void addLine(LineItem line) {
        if (!line.unitPrice().currency().equals(currency)) {
            throw new IllegalArgumentException(
                    "Line " + line.sku() + " is in " + line.unitPrice().currency()
                            + " but invoice " + number + " is in " + currency);
        }
        lines.add(line);
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
}
