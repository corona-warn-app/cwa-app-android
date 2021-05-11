package de.rki.coronawarnapp.util.compression

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ZLIBCompressionTest : BaseTest() {

    @Test
    fun `basic decompression`() {
        ZLIBCompression().decompress(compressed).utf8() shouldBe "The Cake Is A Lie"
    }

    val compressed = "eJwLyUhVcE7MTlXwLFZwVPDJTAUAL3sFLQ==".decodeBase64()!!
}
