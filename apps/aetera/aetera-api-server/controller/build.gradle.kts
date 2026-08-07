plugins {
    id("convention.spring-library")
}

description = "Inbound adapter. Web API / Queue Listener / Scheduler."

dependencies {
    implementation(project(":aetera-usecase"))
    implementation(project(":aetera-model"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.validation)
    // org.springframework.dao 예외 계층용. 낙관적 락 충돌은 트랜잭션 커밋 시점에 터지므로
    // gateway 안에서는 잡을 수 없고, 예외 어드바이스가 유일한 처리 지점이다.
    implementation(libs.spring.tx)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.springdoc.openapi.webmvc)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.springmockk)
}
