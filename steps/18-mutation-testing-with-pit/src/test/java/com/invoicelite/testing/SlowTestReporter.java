package com.invoicelite.testing;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;

/**
 * Reports every test that runs longer than a limit. Register it with @RegisterExtension to choose the limit.
 */
public class SlowTestReporter implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SlowTestReporter.class);
    private static final String START = "start";

    private final Duration limit;

    public SlowTestReporter(Duration limit) {
        this.limit = limit;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(NAMESPACE).put(START, System.nanoTime());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long start = context.getStore(NAMESPACE).remove(START, long.class);
        Duration took = Duration.ofNanos(System.nanoTime() - start);
        if (took.compareTo(limit) > 0) {
            String message = "%s took %d ms (limit %d ms)".formatted(
                    context.getDisplayName(), took.toMillis(), limit.toMillis());
            context.publishReportEntry("slow test", message);
            System.out.println("SLOW: " + message);
        }
    }
}
