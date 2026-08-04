pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "aetera"

/**
 * 서비스 하나를 등록한다. 서비스는 `apps/<제품군>/<서비스>` 아래에 자기만의
 * 클린 아키텍처 스택(model / usecase / gateway / ...)을 갖는 독립 배포 단위다.
 *
 * 디렉터리는 깊게 두되 Gradle 프로젝트 이름은 `:<prefix>-<module>` 로 평평하게 만든다.
 * `project(":apps:aetera:aetera-api-server:model")` 같은 긴 경로를 모든 빌드 파일에
 * 적지 않기 위해서다. prefix 가 서비스마다 다르므로 모듈 이름이 겹치지 않는다.
 */
fun includeService(
    path: String,
    prefix: String,
    vararg modules: String,
) = modules.forEach { module ->
    val name = ":$prefix-$module"
    include(name)
    project(name).projectDir = file("$path/$module")
}

/** 서비스 경계를 넘어 공유하는 모듈. 특정 도메인을 알면 안 된다. */
fun includeSharedLib(vararg names: String) =
    names.forEach { name ->
        include(":$name")
        project(":$name").projectDir = file("libs/shared/$name")
    }

includeSharedLib(
    "shared-core",
)

includeService(
    "apps/aetera/aetera-api-server",
    "aetera",
    "model",
    "usecase",
    "gateway",
    "infrastructure",
    "controller",
    "config",
    "app",
)
