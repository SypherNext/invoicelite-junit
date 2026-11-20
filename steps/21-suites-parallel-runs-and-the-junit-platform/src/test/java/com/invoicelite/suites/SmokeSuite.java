package com.invoicelite.suites;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * A quick check of the money, tax and discount rules: no service, no database, no HTTP, nothing slow.
 * A suite only picks classes whose names end in Test or Tests unless told otherwise, so the property
 * classes ending in Properties are named here.
 */
@Suite
@SuiteDisplayName("Smoke")
@SelectPackages({"com.invoicelite.money", "com.invoicelite.tax", "com.invoicelite.discount"})
@IncludeClassNamePatterns({"^.*Tests?$", "^.*Properties$"})
@ExcludeTags({"slow", "integration"})
public class SmokeSuite {
}
