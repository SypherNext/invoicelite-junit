package com.invoicelite.service;

import java.util.HexFormat;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Makes random keys such as idem-3f9a0c4be21d7781. The random source comes in from outside,
 * so a test can pass a seeded generator and get the same keys every run.
 */
public final class IdempotencyKeys {

    private final RandomGenerator random;

    public IdempotencyKeys(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public String next() {
        return "idem-" + HexFormat.of().toHexDigits(random.nextLong());
    }
}
