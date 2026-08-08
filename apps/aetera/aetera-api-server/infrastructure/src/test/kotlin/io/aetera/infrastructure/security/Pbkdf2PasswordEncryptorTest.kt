package io.aetera.infrastructure.security

import io.aetera.model.auth.EncryptedPassword
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class Pbkdf2PasswordEncryptorTest :
    DescribeSpec({
        val sut = Pbkdf2PasswordEncryptor(iterations = 1_000)

        it("같은 비밀번호라도 매번 다른 해시를 만든다") {
            val first = sut.encrypt("password1234")
            val second = sut.encrypt("password1234")

            first shouldNotBe second
        }

        it("원문 비밀번호를 저장하지 않는다") {
            sut.encrypt("password1234").value.contains("password1234") shouldBe false
        }

        it("올바른 비밀번호를 검증한다") {
            val encrypted = sut.encrypt("password1234")

            sut.matches("password1234", encrypted) shouldBe true
        }

        it("틀린 비밀번호를 거절한다") {
            val encrypted = sut.encrypt("password1234")

            sut.matches("password0000", encrypted) shouldBe false
        }

        it("형식이 깨진 값은 예외 없이 false를 반환한다") {
            sut.matches("password1234", EncryptedPassword("garbage")) shouldBe false
        }
    })
