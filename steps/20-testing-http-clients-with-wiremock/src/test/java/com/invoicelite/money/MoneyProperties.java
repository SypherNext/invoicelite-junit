package com.invoicelite.money;

import com.invoicelite.testing.InvoiceGenerators;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rules that hold for any amounts, checked on generated samples.
 */
class MoneyProperties {

    static Stream<Arguments> pairsOfAmounts() {
        InvoiceGenerators generate = InvoiceGenerators.fromRunSeed();
        return IntStream.range(0, InvoiceGenerators.SAMPLES)
                .mapToObj(i -> Arguments.of(generate.euros(), generate.euros()));
    }

    static Stream<BigDecimal> anyAmounts() {
        InvoiceGenerators generate = InvoiceGenerators.fromRunSeed();
        return IntStream.range(0, InvoiceGenerators.SAMPLES).mapToObj(i -> generate.anyAmount());
    }

    @ParameterizedTest(name = "sample {index}")
    @MethodSource("pairsOfAmounts")
    void additionDoesNotDependOnOrder(Money a, Money b) {
        assertThat(a.plus(b)).isEqualTo(b.plus(a));
    }

    @ParameterizedTest(name = "sample {index}")
    @MethodSource("pairsOfAmounts")
    void subtractingWhatYouAddedGivesTheStartingAmount(Money a, Money b) {
        assertThat(a.plus(b).minus(b)).isEqualTo(a);
    }

    @ParameterizedTest(name = "sample {index}")
    @MethodSource("anyAmounts")
    void everyAmountIsKeptInCents(BigDecimal amount) {
        assertThat(new Money(amount, InvoiceGenerators.EUR).amount().scale()).isEqualTo(2);
    }
}
