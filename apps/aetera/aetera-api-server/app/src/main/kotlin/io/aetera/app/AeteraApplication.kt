package io.aetera.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = [ROOT_PACKAGE])
class AeteraApplication

const val ROOT_PACKAGE: String = "io.aetera"

fun main(args: Array<String>) {
    runApplication<AeteraApplication>(*args)
}
