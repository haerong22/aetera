package io.aetera.app

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * 배포된 **모든** 가이드의 콘텐츠 규칙. 가이드마다 테스트를 복사하면 새 가이드를 추가할 때
 * 빠뜨리기 쉬우므로, [ErrorCodeTest] 와 같은 방식으로 클래스패스에서 직접 수집한다.
 *
 * 이 모듈에서 가장 깨지기 쉬운 것은 로직이 아니라 콘텐츠다 — 항목을 붙여 넣다 키가 겹치거나,
 * 오프셋 부호를 반대로 적거나, 필수 표시를 빠뜨리는 실수는 컴파일러가 잡아주지 않는다.
 */
class GuideContentTest {
    private val guides: List<GuideTemplate> =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages(ROOT_PACKAGE)
            .filter { it.isAssignableTo(GuideModule::class.java) && !it.isInterface }
            .map { it.reflect().getDeclaredConstructor().newInstance() as GuideModule }
            .map { it.template }

    @Test
    fun `가이드를 클래스패스에서 모두 찾는다`() {
        assertThat(guides.map { it.id.value }).contains("resignation", "moving")
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
