package com.invoicelite.examples;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Examples of tests that run only under some conditions.
 */
class ConditionalExamplesTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windowsEndsLinesWithCarriageReturnAndLineFeed() {
        assertThat(System.lineSeparator()).isEqualTo("\r\n");
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void otherSystemsEndLinesWithLineFeed() {
        assertThat(System.lineSeparator()).isEqualTo("\n");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "CI", matches = "true")
    void runsOnlyOnTheBuildServer() {
        assertThat(System.getenv("CI")).isEqualTo("true");
    }

    @Test
    void stopsEarlyWhenAnAssumptionFails() {
        assumeTrue(System.getenv("INVOICELITE_SMTP_HOST") != null, "no mail server configured");

        assertThat(System.getenv("INVOICELITE_SMTP_HOST")).isNotBlank();
    }

    @Test
    @Disabled("Exchange rates are not built yet")
    void convertsDollarsToEuros() {
    }
}
