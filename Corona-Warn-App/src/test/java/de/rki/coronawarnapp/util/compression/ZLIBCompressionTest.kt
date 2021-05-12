package de.rki.coronawarnapp.util.compression

import de.rki.coronawarnapp.util.errors.causes
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.zip.DataFormatException

class ZLIBCompressionTest : BaseTest() {

    @Test
    fun `basic decompression`() {
        ZLIBCompression().decompress(compressed).utf8() shouldBe "The Cake Is A Lie"
    }

    @Test
    fun `invalid decompression`() {
        val error = shouldThrow<InvalidInputException> {
            ZLIBCompression().decompress(compressed.substring(5))
        }
        error.causes().first { it is DataFormatException }.message shouldBe "incorrect header check"
    }

    val compressed = "eJwLyUhVcE7MTlXwLFZwVPDJTAUAL3sFLQ==".decodeBase64()!!
}
