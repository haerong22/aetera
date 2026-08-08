plugins {
    id("convention.spring-library")
}

description = "설정 클래스와 application yml."

dependencies {
    implementation(libs.spring.context)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.springdoc.openapi.webmvc)
}
