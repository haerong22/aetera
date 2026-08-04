// The root project is an aggregator only.
// All shared build configuration lives in `build-logic/src/main/kotlin/convention.*.gradle.kts`
// and all dependency versions live in `gradle/libs.versions.toml`.

tasks.register("verify") {
    group = "verification"
    description = "Runs the full offline check: formatting, compilation and unit tests."
    dependsOn(subprojects.map { "${it.path}:check" })
}
