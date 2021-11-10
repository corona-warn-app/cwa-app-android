package de.rki.coronawarnapp.util.encoding

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class Base45ExtensionsTest : BaseTest() {

    @Test
    fun `encode - extension`() {
        ByteArray(0).base45() shouldBe ""
        ByteArray(1) { 0 }.base45() shouldBe "00"
        "AB".toByteArray().base45() shouldBe "BB8"
        "Hello!!".toByteArray().base45() shouldBe "%69 VD92EX0"
        "base-45".toByteArray().base45() shouldBe "UJCLQE7W581"
        ByteArray(5) { 0 }.base45() shouldBe "00000000"
        ByteArray(7) { -1 }.base45() shouldBe "FGWFGWFGWU5"
        "\uD83E\uDDB8\uD83C\uDFFF\u200D♀️".toByteArray().base45() shouldBe "*IUK3L*IUY7IOSS7.HBIJXDU83"
    }

    @Test
    fun `decode - extension`() {
        "".decodeBase45() shouldBe ByteArray(0)
        "00".decodeBase45() shouldBe ByteArray(1) { 0 }
        "BB8".decodeBase45() shouldBe "AB".toByteArray()
        "%69 VD92EX0".decodeBase45() shouldBe "Hello!!".toByteArray()
        "UJCLQE7W581".decodeBase45() shouldBe "base-45".toByteArray()
        "00000000".decodeBase45() shouldBe ByteArray(5) { 0 }
        "FGWFGWFGWU5".decodeBase45() shouldBe ByteArray(7) { -1 }
        "*IUK3L*IUY7IOSS7.HBIJXDU83".decodeBase45() shouldBe "\uD83E\uDDB8\uD83C\uDFFF\u200D♀️".toByteArray()
    }
}
