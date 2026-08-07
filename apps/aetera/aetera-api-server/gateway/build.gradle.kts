plugins {
    id("convention.jpa")
}

description = "Outbound adapter. model에 정의된 Repository 인터페이스의 구현부와 외부 서버 호출부."

dependencies {
    implementation(project(":aetera-model"))

    implementation(libs.spring.boot.starter.data.jpa)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.spring.boot.starter.flyway)
    runtimeOnly(libs.flyway.postgresql)

    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.bundles.testcontainers)
}
