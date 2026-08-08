package io.aetera.app

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import io.aetera.controller.common.HttpErrorMapper
import io.aetera.controller.common.WebErrorCode
import io.aetera.shared.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * 에러 코드를 도메인별 enum 으로 나눈 대가로 컴파일러가 중복을 잡아주지 못한다. 이 테스트가 그 역할을 대신한다.
 *
 * 대역 자체는 [ErrorCode] 의 companion 이 한 곳에서 관리하므로 중복을 쓰기가 어렵다. 다만 그 상수를
 * 거치지 않고 일련번호를 직접 적어버리는 건 여전히 막을 수 없어서, 검사 대상 목록을 손으로 관리하지 않고
 * **클래스패스에서 직접 수집**한다. 목록에 추가하는 걸 잊어서 검증에서 빠지는 경우도 같이 없앤다.
 *
 * 도메인 코드는 HTTP 를 모르므로, 7자리 값은 [HttpErrorMapper] 를 거쳐야 나온다.
 */
class ErrorCodeTest {
    private val errorCodeEnums: List<Class<*>> =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages(ROOT_PACKAGE)
            .filter { it.isAssignableTo(ErrorCode::class.java) }
            .map { it.reflect() }
            .filter { it.isEnum }

    private val domainErrorCodes: List<ErrorCode> =
        errorCodeEnums
            .flatMap { it.enumConstants?.toList().orEmpty() }
            .filterIsInstance<ErrorCode>()

    private val allCodes: List<Int> =
        domainErrorCodes.map(HttpErrorMapper::codeOf) + WebErrorCode.entries.map { it.code }

    /** enum 상수에 본문이 붙으면 `javaClass` 가 익명 하위 클래스가 되므로 선언 클래스를 되짚는다. */
    private val ErrorCode.declaringClass: Class<*>
        get() = javaClass.let { it.enclosingClass ?: it }

    private val ErrorCode.band: Int
        get() = sequence / ErrorCode.BAND_SIZE

    /**
     * 스캔이 빈 결과를 내면 나머지 테스트가 전부 공허하게 통과한다. 그걸 막는 최소 바닥선이라
     * 도메인을 실제로 없앨 때만 고치면 된다.
     */
    @Test
    fun `에러 코드 enum 을 클래스패스에서 모두 찾는다`() {
        assertThat(errorCodeEnums.map { it.simpleName })
            .contains("UserErrorCode", "AuthErrorCode", "ModuleErrorCode", "ScheduleErrorCode")
    }

    /**
     * 도메인 코드는 서비스의 `model`, 서비스를 가리지 않는 공통 코드는 `libs/shared` 에 둔다.
     * usecase 나 gateway 가 자기 에러 코드를 들고 있으면 계층이 새는 것이다.
     */
    @Test
    fun `에러 코드는 model 이나 공유 모듈에만 산다`() {
        val allowed = listOf("$ROOT_PACKAGE.model", "$ROOT_PACKAGE.shared")

        errorCodeEnums.forEach { type ->
            assertThat(allowed.any { type.packageName.startsWith(it) })
                .withFailMessage { "${type.simpleName} 이 model/shared 밖에 있다: ${type.packageName}" }
                .isTrue()
        }
    }

    @Test
    fun `상세 응답 코드는 전역에서 유일해야 한다`() {
        val duplicated = allCodes.groupBy { it }.filterValues { it.size > 1 }.keys

        assertThat(duplicated)
            .withFailMessage { "중복된 코드: $duplicated" }
            .isEmpty()
    }

    @Test
    fun `상세 응답 코드는 HTTP 상태코드 3자리 + 일련번호 4자리 형식이다`() {
        allCodes.forEach { assertThat(it).isBetween(1_000_000, 5_999_999) }

        domainErrorCodes.forEach {
            assertThat(HttpErrorMapper.codeOf(it) / 10_000).isEqualTo(HttpErrorMapper.statusOf(it.kind).value())
            assertThat(it.sequence).isBetween(1, 9_999)
        }
    }

    /** 두 도메인이 같은 대역 상수를 참조하거나, 등록부를 우회해 번호를 직접 적은 경우를 잡는다. */
    @Test
    fun `도메인과 대역은 1대1 이다`() {
        val bandsByDomain =
            domainErrorCodes
                .groupBy { it.declaringClass.simpleName }
                .mapValues { (_, codes) -> codes.map { it.band }.toSet() }

        bandsByDomain.forEach { (domain, bands) ->
            assertThat(bands)
                .withFailMessage { "$domain 이 여러 대역에 걸쳐 있다: $bands" }
                .hasSize(1)
        }

        val domainsByBand = bandsByDomain.entries.groupBy({ it.value.single() }, { it.key })
        val shared = domainsByBand.filterValues { it.size > 1 }

        assertThat(shared)
            .withFailMessage { "대역이 겹친다: $shared. 이미 쓰는 대역: ${domainsByBand.keys.sorted()}" }
            .isEmpty()
    }

    @Test
    fun `0 번 대역은 프로토콜과 공통 에러 몫이다`() {
        WebErrorCode.entries.forEach {
            assertThat(it.code % 10_000).isBetween(1, ErrorCode.COMMON_BAND - 1)
        }
        domainErrorCodes.filter { it.band == 0 }.forEach {
            assertThat(it.sequence)
                .withFailMessage { "$it 가 controller 의 프로토콜 에러 대역을 침범한다." }
                .isGreaterThanOrEqualTo(ErrorCode.COMMON_BAND)
        }
    }

    @Test
    fun `모든 에러 코드는 메시지를 가진다`() {
        domainErrorCodes.forEach { assertThat(it.defaultMessage).isNotBlank() }
        WebErrorCode.entries.forEach { assertThat(it.defaultMessage).isNotBlank() }
    }

    @Test
    fun `도메인 에러 코드는 HTTP 를 모른다`() {
        domainErrorCodes.forEach {
            assertThat(it.declaringClass.declaredFields.map { field -> field.name })
                .withFailMessage { "$it 가 status 필드를 갖고 있다. HTTP 매핑은 controller 의 책임이다." }
                .doesNotContain("status")
        }
    }
}
