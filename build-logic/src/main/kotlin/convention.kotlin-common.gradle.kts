import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.diffplug.spotless")
}

group = "io.aetera"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(versionCatalog.version("java"))
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            // Treat JSR-305 nullability annotations as strict Kotlin types.
            "-Xjsr305=strict",
            // Kotlin 2.2+: annotations on constructor params apply to param AND property,
            // which is what Jakarta Validation / Jackson expect.
            "-Xannotation-default-target=param-property",
        )
    }
}

dependencies {
    implementation(platform(versionCatalog.lib("kotlin-bom")))
    implementation(versionCatalog.lib("kotlin-logging"))

    testImplementation(platform(versionCatalog.lib("kotest-bom")))
    testImplementation(versionCatalog.bundle("kotest"))
    testImplementation(versionCatalog.lib("mockk"))

    // Both engines everywhere: Kotest specs for the core layers, JUnit Jupiter for anything that
    // needs a Spring context. The `integrationTest` task below filters on the Jupiter engine, which
    // only works if it is present in every module.
    testImplementation(platform(versionCatalog.lib("junit-bom")))
    testImplementation(versionCatalog.lib("junit-jupiter"))
    testRuntimeOnly(versionCatalog.lib("junit-platform-launcher"))
}

val ktlintVersion = versionCatalog.version("ktlint")

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(ktlintVersion)
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(ktlintVersion)
    }
}

// `test` 와 `integrationTest` 가 같은 로그 설정을 쓰도록 한곳에 모읍니다.
// `configureEach` 는 나중에 등록되는 Test 태스크에도 적용됩니다.
tasks.withType<Test>().configureEach {
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        // Docker-backed tests are opt-in so `./gradlew build` stays fast and hermetic.
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs tests tagged 'integration'. Requires a running Docker daemon."
    useJUnitPlatform {
        includeTags("integration")
        // Kotest runs on its own engine and ignores JUnit Platform tag filters, so without this
        // every Kotest spec would run a second time here. Integration tests are JUnit-based.
        includeEngines("junit-jupiter")
    }
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    // Most modules have no integration tests at all; that is not a failure.
    failOnNoDiscoveredTests = false
}
