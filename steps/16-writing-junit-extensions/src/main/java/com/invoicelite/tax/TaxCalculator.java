package com.invoicelite.tax;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Works out the sales tax (VAT) of an invoice from the customer's country.
 */
public final class TaxCalculator {

    private static final Map<String, BigDecimal> RATES = Map.of(
            "DE", new BigDecimal("0.19"),
            "FR", new BigDecimal("0.20"),
            "NL", new BigDecimal("0.21"),
            "IE", new BigDecimal("0.23"));

    private final TaxRounding rounding;

    public TaxCalculator(TaxRounding rounding) {
        this.rounding = Objects.requireNonNull(rounding, "rounding");
    }

    public BigDecimal rateFor(String countryCode) {
        BigDecimal rate = countryCode == null ? null : RATES.get(countryCode);
        if (rate == null) {
            throw new IllegalArgumentException("No tax rate for country " + countryCode);
        }
        return rate;
    }

    public Money taxFor(Invoice invoice) {
        return taxFor(invoice, Money.zero(invoice.currency()));
    }

    /**
     * Tax on the lines, less the tax on the discount (a discount works like a negative line).
     */
    public Money taxFor(Invoice invoice, Money discount) {
        BigDecimal rate = rateFor(invoice.countryCode());
        Money tax = discount.times(rate).negated();
        Money base = discount.negated();
        for (LineItem line : invoice.lines()) {
            tax = tax.plus(line.total().times(rate));
            base = base.plus(line.total());
        }
        return rounding == TaxRounding.PER_LINE ? tax : base.times(rate);
    }
}
