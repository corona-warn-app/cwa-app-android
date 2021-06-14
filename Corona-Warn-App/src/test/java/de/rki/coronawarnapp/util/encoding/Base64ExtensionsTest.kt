package de.rki.coronawarnapp.util.encoding

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class Base64ExtensionsTest : BaseTest() {

    @Test
    fun `bytearray conversion`() {
        "I'mOnMyFirstCoffee".toByteArray().base64() shouldBe "SSdtT25NeUZpcnN0Q29mZmVl"
    }
}
