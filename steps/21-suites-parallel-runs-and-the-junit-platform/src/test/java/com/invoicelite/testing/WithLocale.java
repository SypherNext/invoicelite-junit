package com.invoicelite.testing;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs each test with this default locale (a language tag such as de-DE) and puts the old one back afterwards.
 * The resource lock stops other tests that use the default locale from running at the same time.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DefaultLocaleExtension.class)
@ResourceLock(Resources.LOCALE)
public @interface WithLocale {

    String value();
}
