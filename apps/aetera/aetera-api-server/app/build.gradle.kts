import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("convention.spring-boot-app")
}

description = "Application 정의. 필요한 모듈을 gradle module로 임포트만 한다."

dependencies {
    implementation(project(":aetera-config"))
    implementation(project(":aetera-controller"))
    implementation(project(":aetera-gateway"))
    implementation(project(":aetera-infrastructure"))
    implementation(project(":aetera-usecase"))
    implementation(project(":aetera-model"))

    implementation(libs.spring.boot.starter.actuator)
    runtimeOnly(libs.micrometer.registry.prometheus)

    developmentOnly(platform(libs.spring.boot.dependencies))
    developmentOnly(libs.spring.boot.docker.compose)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.archunit.junit5)
}

tasks.named<BootRun>("bootRun") {
    workingDir = rootDir
    systemProperty("spring.profiles.active", "local")
}
