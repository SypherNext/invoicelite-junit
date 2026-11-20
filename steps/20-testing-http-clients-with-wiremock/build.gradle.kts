import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    jacoco
    id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.invoicelite"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val mockitoVersion = "5.24.0"
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation("org.postgresql:postgresql:42.7.14")
    implementation("tools.jackson.core:jackson-databind:3.3.0")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.wiremock:wiremock-standalone:3.13.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.20")
    mockitoAgent("org.mockito:mockito-core:$mockitoVersion") { isTransitive = false }
}

// Loads Mockito as a Java agent, as the Mockito documentation recommends for Java 21 and later.
abstract class MockitoAgentProvider : CommandLineArgumentProvider {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentJar: ConfigurableFileCollection

    override fun asArguments(): Iterable<String> = listOf("-javaagent:${agentJar.asPath}")
}

// Settings shared by every test task: the Mockito agent and the console output.
tasks.withType<Test>().configureEach {
    // Property tests print their random seed; repeat a run with: ./gradlew test -PpropertySeed=<seed>
    providers.gradleProperty("propertySeed").orNull?.let { systemProperty("invoicelite.seed", it) }
    jvmArgumentProviders.add(objects.newInstance<MockitoAgentProvider>().apply {
        agentJar.from(mockitoAgent)
    })
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

// Fast tests: everything except the tests tagged "integration".
tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
        // Run a subset with a tag expression, for example: ./gradlew test -Ptags='!slow'
        providers.gradleProperty("tags").orNull?.let { includeTags(it) }
    }
}

// Slower tests that need Docker: ./gradlew integrationTest
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs the tests tagged integration. They start containers, so Docker must be running."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTest)
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.jacocoTestCoverageVerification)
}

jacoco {
    toolVersion = "0.8.16"
}

// Coverage from both test tasks: the JDBC repository is only exercised by the integration tests.
tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest)
    executionData(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
    reports {
        xml.required = true
        csv.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test, integrationTest)
    executionData(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.90".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

pitest {
    pitestVersion = "1.30.2"
    junit5PluginVersion = "1.2.3"
    targetClasses = listOf("com.invoicelite.*")
    excludedGroups = listOf("integration")
    excludedClasses = listOf("com.invoicelite.persistence.*")
    threads = 4
    outputFormats = listOf("HTML", "XML")
    timestampedReports = false
    mutationThreshold = 95
    jvmArgs.add(mockitoAgent.elements.map { "-javaagent:" + it.single().asFile.absolutePath })
}
