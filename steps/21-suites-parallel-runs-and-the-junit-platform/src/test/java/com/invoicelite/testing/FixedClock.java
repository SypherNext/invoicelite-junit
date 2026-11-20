package com.invoicelite.testing;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gives a test method a java.time.Clock parameter that is fixed at this instant.
 * Put it on a method, or on the class to use the same instant for every test.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FixedClockExtension.class)
public @interface FixedClock {

    /** An instant such as 2026-03-15T09:00:00Z. */
    String value();

    /** The time zone of the clock. */
    String zone() default "Europe/Berlin";
}
