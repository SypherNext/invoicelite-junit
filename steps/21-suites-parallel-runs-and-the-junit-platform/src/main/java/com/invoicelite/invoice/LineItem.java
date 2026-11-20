package com.invoicelite.invoice;

import com.invoicelite.money.Money;

import java.util.Objects;

/**
 * One line of an invoice: what was sold, at what unit price, and how many.
 */
public record LineItem(String sku, String description, Money unitPrice, int quantity) {

    public LineItem {
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1, was " + quantity);
        }
    }

    public Money total() {
        return unitPrice.times(quantity);
    }
}
