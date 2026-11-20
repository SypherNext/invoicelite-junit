package com.invoicelite.testing;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Resolves Clock parameters from the nearest @FixedClock: on the method first, then on the class.
 */
public class FixedClockExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Clock.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        FixedClock fixedClock = extensionContext.getTestMethod()
                .flatMap(method -> AnnotationSupport.findAnnotation(method, FixedClock.class))
                .or(() -> AnnotationSupport.findAnnotation(extensionContext.getRequiredTestClass(), FixedClock.class))
                .orElseThrow(() -> new ParameterResolutionException("A Clock parameter needs @FixedClock"));
        return Clock.fixed(Instant.parse(fixedClock.value()), ZoneId.of(fixedClock.zone()));
    }
}
