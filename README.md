# InvoiceLite: JUnit companion code

InvoiceLite is a small invoicing library that you test from the first `@Test` to a capstone feature in the course
**JUnit 6: Unit Testing in Java**. This repository holds a runnable copy of the project at the end of every lesson
that changes code, plus the finished project.

```text
steps/<NN>-<lesson-slug>/   the project at the end of lesson NN (a complete Gradle project)
final/                      the finished project, the same as the last step
scripts/verify-all.sh       builds and tests every step and final (verify-all.ps1 for PowerShell)
```

Every folder builds on its own. If something you typed does not work, compare it with the step folder of that
lesson.

## Set up your computer

You need **JDK 25**, **Docker** (from lesson 19 on) and **an editor**. Gradle comes with every project (the Gradle
wrapper, `gradlew`), so you do not install it yourself.

### JDK 25

The projects compile and run with Java 25 (Gradle's toolchain asks for it). Any JDK 25 works; the course uses
Eclipse Temurin.

| System | Command |
| --- | --- |
| Windows (PowerShell) | `winget install EclipseAdoptium.Temurin.25.JDK` |
| macOS (Homebrew) | `brew install --cask temurin@25` |
| Ubuntu or Debian | add the Adoptium apt repository (see the Adoptium installation guide), then `sudo apt install temurin-25-jdk` |

Open a new terminal and check:

```bash
java -version
```

It must print `25` in the first line. If an older Java comes first on your PATH, set `JAVA_HOME` to the JDK 25
folder, for example:

```bash
# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
# Linux (the path depends on your distribution)
export JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64
```

```powershell
# Windows PowerShell (the path depends on the installer)
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot'
```

### Docker

From lesson 19 the integration tests start PostgreSQL in a throwaway container with Testcontainers.

- Windows and macOS: install Docker Desktop and start it.
- Linux: install Docker Engine and add your user to the `docker` group.

Check with `docker version`. Docker must be running before you build step 19 or later, because `./gradlew build`
runs the integration tests too.

### An editor

IntelliJ IDEA (Community Edition is free) or Visual Studio Code with the Extension Pack for Java. Open a project
folder (for example `final`), not the repository root; the editor reads `build.gradle.kts` by itself.

## Run any step or final

From the folder of the project you want, for example `final`:

```bash
./gradlew build             # compile, run every test, check coverage
./gradlew test              # only the fast tests (no Docker needed)
./gradlew integrationTest   # only the tests that need Docker (from step 19)
./gradlew pitest            # mutation testing (from step 18), report in build/reports/pitest/
```

On Windows in PowerShell or cmd, type `.\gradlew.bat` where this file writes `./gradlew`.

The HTML reports are in `build/reports/`: `tests/test/index.html` for the test results and
`jacoco/test/html/index.html` for coverage (from step 15).

## Course map

| Step | Lesson | What the project gains |
| --- | --- | --- |
| 03 | Your First JUnit Test | Money with plus and times, the first four tests |
| 04 | Arrange, Act, Assert and Test Names | LineItem, Invoice, InvoiceCalculator.subtotal, readable test names |
| 05 | Assertions in Depth with AssertJ | AssertJ assertions, soft assertions, Invoice.lineFor |
| 06 | The Test Lifecycle | TaxCalculator with per line or per invoice rounding, shared test data |
| 07 | Writing Testable Code | invoice numbers from an injected Clock and sequence, issue and due dates |
| 08 | Test-Driven Development | three discount policies built test first, invoice totals |
| 09 | Parameterized Tests | tax rates, rounding and tier boundaries as tables of cases |
| 10 | Nested, Tagged and Conditional Tests | invoice lifecycle (paid, overdue) tested with nested classes, tags |
| 11 | Dynamic and Repeated Tests | golden invoices from a CSV file, a thread-safe sequence |
| 12 | Test Doubles with Mockito | InvoiceService with a repository, a payment gateway and a notifier |
| 13 | Mockito in Depth: Matchers, Captors and What Not to Mock | argument matchers, captors, in-order checks, spies |
| 14 | Testing Time, Randomness and Locale | a test clock, seeded idempotency keys, money formatted per locale |
| 15 | Code Coverage with JaCoCo | coverage report and minimum coverage rules |
| 16 | Writing JUnit Extensions | @FixedClock, @WithLocale and a slow test reporter |
| 17 | Property-Based Testing in Plain JUnit | a seeded generator, properties for money, discounts and tax, a small shrinker; a bug found and fixed |
| 18 | Mutation Testing with PIT | the pitest task and the tests that kill every mutant |
| 19 | Integration Tests with Testcontainers | a PostgreSQL repository, one contract test for both repositories |
| 20 | Testing HTTP Clients with WireMock | an HTTP client for the payment provider |
| 21 | Suites, Parallel Runs and the JUnit Platform | a smoke suite, parallel test classes, resource locks |
| 22 | Tests in CI, Flaky Tests and Test Smells | a GitHub Actions workflow, random test order with a printed seed, timeouts |
| 23 | Capstone: A Feature Built Test First | part payments, from unit test to database and HTTP |

Lessons 00 to 02 and 24 change no code, so they have no step folder.

## Check everything

`scripts/verify-all.sh` (macOS, Linux, Git Bash) and `scripts/verify-all.ps1` (Windows PowerShell) build and test
every step folder and `final`. Add `--clean` or `-Clean` to delete each project's `build/` and `.gradle/` folders
afterwards. Docker must be running for steps 19 to 23 and `final`.

## License

Copyright (C) 2026 SypherNext

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0). You may
use, study and change it. If you share a modified version, or run one as a website or service
that other people use, you must make your full modified source code available under the same
license. See [LICENSE](LICENSE) for the full terms.
