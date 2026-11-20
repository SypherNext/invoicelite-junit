package com.invoicelite.discount;

import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.invoicelite.invoice.TestInvoices.line;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BuyNGetOneFreeTest {

    private final DiscountPolicy buyThreeMugsGetOneFree = new BuyNGetOneFree("MUG-01", 3);

    @Test
    void noDiscountWhenTheSkuIsNotOnTheInvoice() {
        List<LineItem> lines = List.of(line("PEN-10", "6.40", 5));

        Money discount = buyThreeMugsGetOneFree.discountFor(lines, Money.of("32.00", "EUR"));

        assertThat(discount).isEqualTo(Money.of("0.00", "EUR"));
    }

    @Test
    void theFourthMugIsFree() {
        List<LineItem> lines = List.of(line("MUG-01", "12.50", 4));

        Money discount = buyThreeMugsGetOneFree.discountFor(lines, Money.of("50.00", "EUR"));

        assertThat(discount).isEqualTo(Money.of("12.50", "EUR"));
    }

    @Test
    void countsMugsAcrossSeveralLines() {
        List<LineItem> lines = List.of(line("MUG-01", "12.50", 2), line("MUG-01", "12.50", 2));

        Money discount = buyThreeMugsGetOneFree.discountFor(lines, Money.of("50.00", "EUR"));

        assertThat(discount).isEqualTo(Money.of("12.50", "EUR"));
    }

    @Test
    void theCheapestMugsAreTheFreeOnes() {
        // Mugs at two prices: 12 mugs, 3 free, and the free ones must be the 0.01 mugs
        List<LineItem> lines = List.of(line("MUG-01", "0.06", 1), line("MUG-01", "0.01", 11));

        Money discount = buyThreeMugsGetOneFree.discountFor(lines, Money.of("0.17", "EUR"));

        assertThat(discount).isEqualTo(Money.of("0.03", "EUR"));
    }

    @Test
    void eightMugsGiveTwoFree() {
        List<LineItem> lines = List.of(line("MUG-01", "12.50", 8));

        Money discount = buyThreeMugsGetOneFree.discountFor(lines, Money.of("100.00", "EUR"));

        assertThat(discount).isEqualTo(Money.of("25.00", "EUR"));
    }

    @Test
    void refusesFewerThanOnePaidUnit() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BuyNGetOneFree("MUG-01", 0))
                .withMessage("Paid units must be at least 1, was 0");
    }
}
