package com.invoicelite.testing;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Locale;

/**
 * Sets the default locale before each test and restores the original one after it,
 * even when the test fails.
 */
public class DefaultLocaleExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DefaultLocaleExtension.class);
    private static final String ORIGINAL = "original locale";

    @Override
    public void beforeEach(ExtensionContext context) {
        WithLocale withLocale = context.getTestMethod()
                .flatMap(method -> AnnotationSupport.findAnnotation(method, WithLocale.class))
                .or(() -> AnnotationSupport.findAnnotation(context.getRequiredTestClass(), WithLocale.class))
                .orElseThrow();
        context.getStore(NAMESPACE).put(ORIGINAL, Locale.getDefault());
        Locale.setDefault(Locale.forLanguageTag(withLocale.value()));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Locale original = context.getStore(NAMESPACE).remove(ORIGINAL, Locale.class);
        Locale.setDefault(original);
    }
}
