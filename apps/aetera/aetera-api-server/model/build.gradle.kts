plugins {
    id("convention.kotlin-common")
}

description = "도메인 모델과 도메인 규칙. 스프링 빈을 만들지 않고 서드파티 의존도 두지 않는다."

dependencies {
    // Page/Slice 처럼 도메인을 모르는 공유 타입. 아웃바운드 포트 시그니처에 노출되므로 api 로 뺀다.
    api(project(":shared-core"))
}
