plugins {
    id("convention.kotlin-common")
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    // Exposed as `api` so every consuming module inherits the same managed versions
    // without having to re-declare the platform.
    api(platform(versionCatalog.lib("spring-boot-dependencies")))

    implementation(versionCatalog.lib("kotlin-reflect"))

    testImplementation(versionCatalog.lib("spring-boot-starter-test"))
}
