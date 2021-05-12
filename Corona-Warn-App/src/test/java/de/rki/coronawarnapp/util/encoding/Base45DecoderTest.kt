package de.rki.coronawarnapp.util.encoding

import io.kotest.matchers.shouldBe
import okio.internal.commonAsUtf8ToByteArray
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class Base45DecoderTest : BaseTest() {

    @Test
    fun `encode - direct`() {
        Base45Decoder.encode("AB".toByteArray()) shouldBe "BB8"
        Base45Decoder.encode("Hello!!".commonAsUtf8ToByteArray()) shouldBe "%69 VD92EX0"
        Base45Decoder.encode("base-45".commonAsUtf8ToByteArray()) shouldBe "UJCLQE7W581"
    }

    @Test
    fun `decode - direct`() {
        Base45Decoder.decode("BB8") shouldBe "AB".toByteArray()
        Base45Decoder.decode("%69 VD92EX0") shouldBe "Hello!!".toByteArray()
        Base45Decoder.decode("UJCLQE7W581") shouldBe "base-45".toByteArray()
    }
}
