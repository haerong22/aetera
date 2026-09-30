package io.aetera.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * `@ConfigurationPropertiesScan` 이 없으면 `@ConfigurationProperties` 클래스가 빈으로
 * 뜨지 않아 **주입 지점에서 기동이 실패한다.** 애너테이션만 붙여 두고 등록을 빠뜨리기 쉬운 자리다.
 */
@SpringBootApplication(scanBasePackages = [ROOT_PACKAGE])
@ConfigurationPropertiesScan(ROOT_PACKAGE)
class AeteraApplication

const val ROOT_PACKAGE: String = "io.aetera"

fun main(args: Array<String>) {
    runApplication<AeteraApplication>(*args)
}
