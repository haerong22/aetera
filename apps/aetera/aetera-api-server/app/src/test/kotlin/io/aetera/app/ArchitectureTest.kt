package io.aetera.app

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.CompositeArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * 모듈 도메인의 패키지 이름. 새 모듈을 추가하면 **여기 한 줄만** 늘린다 —
 * 규칙은 이 목록에서 모든 조합을 만들어 내므로 손대지 않는다.
 *
 * 예전에는 "schedule 이 아직 없는 패키지들을 참조하지 않는다"로 적혀 있었는데,
 * 대상이 비어 있으면 규칙이 언제나 통과해서 아무것도 지키지 않았다.
 */
private val MODULE_PACKAGES = listOf("schedule", "guide", "renewal", "goal", "expense", "asset")

@AnalyzeClasses(
    packages = ["io.aetera"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ArchitectureTest {
    @ArchTest
    val modelUsesOnlyItsOwnPackage: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.model..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "$ROOT_PACKAGE.usecase..",
                "$ROOT_PACKAGE.gateway..",
                "$ROOT_PACKAGE.infrastructure..",
                "$ROOT_PACKAGE.controller..",
                "$ROOT_PACKAGE.config..",
                "$ROOT_PACKAGE.app..",
            ).because("model은 현재 패키지에 포함된 클래스만 사용한다")

    @ArchTest
    val modelHasNoFrameworkDependency: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.model..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "jakarta.validation..",
                "jakarta.servlet..",
                "org.hibernate..",
                "tools.jackson..",
                "com.fasterxml.jackson..",
                "com.nimbusds..",
            ).because("model은 스프링 빈으로 생성하지 않고 서드파티 의존을 두지 않는다")

    @ArchTest
    val usecaseUsesOnlyModelAndItself: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.usecase..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "$ROOT_PACKAGE.gateway..",
                "$ROOT_PACKAGE.infrastructure..",
                "$ROOT_PACKAGE.controller..",
                "$ROOT_PACKAGE.config..",
                "$ROOT_PACKAGE.app..",
            ).because("usecase는 현재 패키지와 model 패키지만 사용한다")

    @ArchTest
    val usecaseIsFreeOfWebAndPersistenceTechnology: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.usecase..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "jakarta.persistence..",
                "jakarta.servlet..",
                "org.springframework.web..",
                "org.springframework.data..",
                "org.hibernate..",
            ).because("usecase는 특정 웹/영속성 기술에 묶이지 않는다")

    @ArchTest
    val gatewayUsesOnlyModelAndItself: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.gateway..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "$ROOT_PACKAGE.usecase..",
                "$ROOT_PACKAGE.infrastructure..",
                "$ROOT_PACKAGE.controller..",
                "$ROOT_PACKAGE.config..",
                "$ROOT_PACKAGE.app..",
            ).because("gateway는 현재 패키지에 정의된 클래스와 model 클래스만 사용한다")

    @ArchTest
    val infrastructureUsesOnlyModelAndItself: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "$ROOT_PACKAGE.usecase..",
                "$ROOT_PACKAGE.gateway..",
                "$ROOT_PACKAGE.controller..",
                "$ROOT_PACKAGE.config..",
                "$ROOT_PACKAGE.app..",
            ).because("infrastructure는 현재 패키지에 정의된 클래스와 model 클래스만 사용한다")

    @ArchTest
    val controllerUsesOnlyUsecaseAndModel: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.controller..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "$ROOT_PACKAGE.gateway..",
                "$ROOT_PACKAGE.infrastructure..",
                "$ROOT_PACKAGE.config..",
                "$ROOT_PACKAGE.app..",
            ).because("controller는 usecase, controller, model 패키지의 클래스만 사용한다")

    @ArchTest
    val controllersDoNotReferenceEachOther: ArchRule =
        noClasses()
            .that()
            .haveSimpleNameEndingWith("Controller")
            .should()
            .dependOnClassesThat(
                JavaClass.Predicates
                    .simpleNameEndingWith("Controller")
                    .and(JavaClass.Predicates.resideInAPackage("$ROOT_PACKAGE.controller..")),
            ).because("controller 클래스는 서로 참조할 수 없다")

    @ArchTest
    val sharedLibsDoNotDependOnServices: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.shared..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "$ROOT_PACKAGE.model..",
                "$ROOT_PACKAGE.usecase..",
                "$ROOT_PACKAGE.gateway..",
                "$ROOT_PACKAGE.infrastructure..",
                "$ROOT_PACKAGE.controller..",
                "$ROOT_PACKAGE.config..",
                "$ROOT_PACKAGE.app..",
            ).because("libs/shared 는 여러 서비스가 함께 쓰므로 특정 서비스를 알면 안 된다")

    @ArchTest
    val repositoryInterfacesDoNotLiveInUsecase: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("$ROOT_PACKAGE.usecase..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("Repository")
            .because("Repository 인터페이스는 model 패키지에 정의한다")
            .allowEmptyShould(true)

    // ── Aetera 모듈 계약 ──────────────────────────────────────────────────
    // 모듈(schedule, guide, 앞으로 올 budget, ...)은 서로를 몰라야 한다.
    // 새 모듈 도메인을 추가하면 [MODULE_PACKAGES] 에 한 줄 추가한다 — 규칙 자체는 바뀌지 않는다.

    @ArchTest
    val aeteraModulesDoNotReferenceEachOther: ArchRule =
        CompositeArchRule
            .of(
                MODULE_PACKAGES.map { module ->
                    noClasses()
                        .that()
                        .resideInAPackage("$ROOT_PACKAGE..$module..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(*(MODULE_PACKAGES - module).map { "$ROOT_PACKAGE..$it.." }.toTypedArray())
                        .allowEmptyShould(true)
                },
            ).because("모듈은 다른 모듈의 클래스를 직접 참조할 수 없다 — 모듈 간 협력이 필요하면 코어를 거친다")
}
