package com.invoicelite.testing;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs each test with this default locale (a language tag such as de-DE) and puts the old one back afterwards.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DefaultLocaleExtension.class)
public @interface WithLocale {

    String value();
}
