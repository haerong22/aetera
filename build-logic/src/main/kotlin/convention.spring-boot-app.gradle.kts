plugins {
    id("convention.spring-library")
    id("org.springframework.boot")
}

// Only the executable jar is published from an application module.
tasks.named<Jar>("jar") {
    enabled = false
}
