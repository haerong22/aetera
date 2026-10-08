package io.aetera.app

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Component
import java.lang.reflect.Modifier
import java.time.LocalDate

/**
 * 배포된 **모든** 가이드의 콘텐츠 규칙. 가이드마다 테스트를 복사하면 새 가이드를 추가할 때
 * 빠뜨리기 쉬우므로, [ErrorCodeTest] 와 같은 방식으로 클래스패스에서 직접 수집한다.
 *
 * 이 모듈에서 가장 깨지기 쉬운 것은 로직이 아니라 콘텐츠다 — 항목을 붙여 넣다 키가 겹치거나,
 * 오프셋 부호를 반대로 적거나, 필수 표시를 빠뜨리는 실수는 컴파일러가 잡아주지 않는다.
 */
class GuideContentTest {
    private val guideModules: List<Class<*>> =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages(ROOT_PACKAGE)
            .filter { it.isAssignableTo(GuideModule::class.java) && !it.isInterface }
            .map { it.reflect() }

    private val guides: List<GuideTemplate> =
        guideModules.map { (it.getDeclaredConstructor().newInstance() as GuideModule).template }

    /**
     * 아이디를 손으로 늘어놓지 않는다 — 새 가이드를 추가할 때 그 목록을 고치는 걸 잊으면
     * 이 파일이 없애려던 "가이드마다 손으로 관리하는 목록"이 그대로 생긴다.
     *
     * 대신 새 가이드에서 실제로 나는 실수를 본다: 콘텐츠를 다 써 놓고 `@Component` 를 빠뜨리면
     * **아래 규칙 테스트는 전부 통과하는데** 카탈로그에는 안 들어가 화면에서만 사라진다.
     */
    @Test
    fun `클래스패스에서 찾은 가이드는 전부 빈으로 등록되어 있다`() {
        assertThat(guides).isNotEmpty

        guideModules.forEach { module ->
            assertThat(module.isAnnotationPresent(Component::class.java))
                .withFailMessage { "가이드 '${module.simpleName}' 에 @Component 가 없어 카탈로그에 등록되지 않는다" }
                .isTrue()
        }
    }

    /**
     * 콘텐츠 파일만 쓰고 [GuideModule] 클래스를 빠뜨리면 **그 가이드는 아무 시험에도 걸리지 않는다.**
     * 위 규칙들은 모듈 빈에서 템플릿을 모으므로, 모듈이 없으면 애초에 수집되지 않아 전부 통과한다.
     * 화면에서만 조용히 사라진다 — 실제로 한 번 그렇게 만들었다.
     *
     * 그래서 모듈이 아니라 **콘텐츠 쪽에서** 센다. 코틀린은 파일 최상단 `val` 을
     * `파일이름Kt` 클래스의 정적 필드로 만들므로, 거기 있는 [GuideTemplate] 을 전부 찾아
     * 모듈이 들고 있는 것과 맞춰 본다.
     */
    @Test
    fun `콘텐츠 파일마다 모듈이 있다`() {
        val declaredTemplates =
            ClassFileImporter()
                .withImportOption(ImportOption.DoNotIncludeTests())
                .importPackages(ROOT_PACKAGE)
                .filter { javaClass -> javaClass.fields.any { it.rawType.name == GuideTemplate::class.java.name } }
                .flatMap { javaClass ->
                    javaClass
                        .reflect()
                        .declaredFields
                        /*
                         * **정적 필드만 본다.** `GuideModule` 구현의 `override val template` 도 같은
                         * 타입이지만 인스턴스 필드라 `get(null)` 이 NPE 를 낸다 — 터뜨리고 알았다.
                         * 찾으려는 것은 파일 최상단 `val`, 즉 정적 필드뿐이다.
                         */
                        .filter { it.type == GuideTemplate::class.java && Modifier.isStatic(it.modifiers) }
                        .map { field ->
                            field.isAccessible = true
                            javaClass.simpleName to (field.get(null) as GuideTemplate).id
                        }
                }

        val registered = guides.map { it.id }.toSet()
        val orphans = declaredTemplates.filterNot { it.second in registered }

        assertThat(orphans)
            .withFailMessage {
                "콘텐츠는 있는데 GuideModule 이 없다: " +
                    orphans.joinToString { "${it.first}(${it.second})" } +
                    " — @Component 붙은 GuideModule 구현을 추가해야 카탈로그에 들어간다"
            }.isEmpty()
    }

    @Test
    fun `할 일 키는 가이드 안에서 유일하다`() {
        guides.forEach { guide ->
            val duplicated =
                guide.tasks
                    .groupingBy { it.key }
                    .eachCount()
                    .filterValues { it > 1 }
                    .keys
            assertThat(duplicated)
                .withFailMessage { "가이드 '${guide.id}' 의 키가 겹친다: $duplicated" }
                .isEmpty()
        }
    }

    @Test
    fun `할 일은 마감이 이른 것부터 나온다`() {
        guides.forEach { guide ->
            val offsets = guide.tasks.map { it.dueOffsetDays }
            assertThat(offsets)
                .withFailMessage { "가이드 '${guide.id}' 의 할 일 순서가 시간 순이 아니다: $offsets" }
                .isSorted
        }
    }

    @Test
    fun `단계는 비어 있지 않고 모든 할 일이 어느 단계엔가 속한다`() {
        guides.forEach { guide ->
            assertThat(guide.phases).isNotEmpty
            guide.phases.forEach { phase ->
                assertThat(phase.tasks)
                    .withFailMessage { "가이드 '${guide.id}' 의 단계 '${phase.key}' 가 비어 있다" }
                    .isNotEmpty
            }
        }
    }

    @Test
    fun `필수 항목이 있고 전부 필수는 아니다`() {
        guides.forEach { guide ->
            assertThat(guide.requiredTaskCount)
                .withFailMessage { "가이드 '${guide.id}' 에 필수 항목이 없다" }
                .isGreaterThan(0)
            assertThat(guide.tasks.size)
                .withFailMessage { "가이드 '${guide.id}' 는 전부 필수라 진행률이 지표가 못 된다" }
                .isGreaterThan(guide.requiredTaskCount)
        }
    }

    @Test
    fun `모든 항목이 제목과 설명을 갖는다`() {
        guides.flatMap { it.tasks }.forEach {
            assertThat(it.title).isNotBlank()
            assertThat(it.description).isNotBlank()
        }
    }

    @Test
    fun `가이드는 제목과 기준일 이름과 고지를 갖는다`() {
        guides.forEach {
            assertThat(it.title).isNotBlank()
            assertThat(it.summary).isNotBlank()
            assertThat(it.anchorLabel).isNotBlank()
            assertThat(it.disclaimer).isNotBlank()
        }
    }

    /**
     * 화면은 설명을 **글자 그대로** 그린다(`{task.description}`) — 마크다운을 쓰지 않는다.
     * 그래서 `**강조**` 를 적으면 별표가 그대로 보인다. 콘텐츠를 쓰다 KDoc 의 습관이
     * 설명문으로 넘어오기 쉬운 자리라 여기서 막는다.
     */
    @Test
    fun `설명에 마크다운 표기를 쓰지 않는다`() {
        guides.forEach { guide ->
            guide.tasks.forEach { task ->
                assertThat(task.description)
                    .withFailMessage {
                        "가이드 '${guide.id}' 의 '${task.key}' 설명에 마크다운이 있다 — 화면은 글자 그대로 그린다"
                    }.doesNotContain("**")
                    .doesNotContain("](")
            }
        }
    }

    @Test
    fun `링크는 https 로만 건다`() {
        guides.flatMap { it.tasks }.mapNotNull { it.link }.forEach {
            assertThat(it.url).startsWith("https://")
            assertThat(it.label).isNotBlank()
        }
    }

    @Test
    fun `기준일을 넣으면 마감이 내 달력의 실제 날짜가 된다`() {
        val anchor = LocalDate.of(2026, 9, 30)

        guides.flatMap { it.tasks }.forEach {
            assertThat(it.dueDateFrom(anchor)).isEqualTo(anchor.plusDays(it.dueOffsetDays.toLong()))
        }
    }
}
