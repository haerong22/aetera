plugins {
    id("convention.spring-library")
}

description = "인프라 상세 구현부(암호화, 토큰 서명 등). model에 정의된 인터페이스를 구현한다."

dependencies {
    implementation(project(":aetera-model"))

    implementation(libs.spring.context)
    implementation(libs.nimbus.jose.jwt)
}
