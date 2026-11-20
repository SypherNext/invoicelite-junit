package com.invoicelite.money;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Shows an amount the way people in one locale write it: 1.234,50 € in Germany, €1,234.50 in the US.
 */
public final class MoneyFormatter {

    private final Locale locale;

    public MoneyFormatter(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale");
    }

    /** Uses the computer's default format locale. Prefer passing a locale. */
    public static MoneyFormatter forDefaultLocale() {
        return new MoneyFormatter(Locale.getDefault(Locale.Category.FORMAT));
    }

    public String format(Money money) {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setCurrency(money.currency());
        return format.format(money.amount());
    }
}
