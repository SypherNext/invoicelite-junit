package com.invoicelite.service;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyKeysTest {

    @Test
    void keysHaveTheExpectedShape() {
        IdempotencyKeys keys = new IdempotencyKeys(new Random());

        assertThat(keys.next()).matches("idem-[0-9a-f]{16}");
    }

    @Test
    void theSameSeedGivesTheSameKeys() {
        IdempotencyKeys first = new IdempotencyKeys(new Random(42));
        IdempotencyKeys second = new IdempotencyKeys(new Random(42));

        assertThat(first.next()).isEqualTo(second.next());
        assertThat(first.next()).isEqualTo(second.next());
    }

    @Test
    void aSeededGeneratorGivesAKnownKey() {
        IdempotencyKeys keys = new IdempotencyKeys(new Random(42));

        assertThat(keys.next()).isEqualTo("idem-ba419d350dfe8af7");
    }
}
