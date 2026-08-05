plugins {
    id("convention.spring-library")
}

description = "Application Service. 도메인 객체를 사용해 비즈니스 로직을 실행한다."

dependencies {
    api(project(":aetera-model"))

    implementation(libs.spring.context)
    implementation(libs.spring.tx)
}
