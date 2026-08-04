plugins {
    id("convention.spring-library")
    id("org.jetbrains.kotlin.plugin.jpa")
}

// `plugin.spring` already opens @Component & friends; JPA needs the same treatment
// because Hibernate proxies entity classes.
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
