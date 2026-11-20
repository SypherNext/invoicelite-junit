import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
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
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    mockitoAgent("org.mockito:mockito-core:$mockitoVersion") { isTransitive = false }
}

// Loads Mockito as a Java agent, as the Mockito documentation recommends for Java 21 and later.
abstract class MockitoAgentProvider : CommandLineArgumentProvider {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentJar: ConfigurableFileCollection

    override fun asArguments(): Iterable<String> = listOf("-javaagent:${agentJar.asPath}")
}

tasks.withType<Test>().configureEach {
    jvmArgumentProviders.add(objects.newInstance<MockitoAgentProvider>().apply {
        agentJar.from(mockitoAgent)
    })
}

tasks.test {
    useJUnitPlatform {
        // Run a subset with a tag expression, for example: ./gradlew test -Ptags='!slow'
        providers.gradleProperty("tags").orNull?.let { includeTags(it) }
    }
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
