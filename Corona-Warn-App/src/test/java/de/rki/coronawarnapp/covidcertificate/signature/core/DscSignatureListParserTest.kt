package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.covidcertificate.signature.core.DscRawData.DSC_LIST_BASE64
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.Test

@Suppress("MaxLineLength")
internal class DscSignatureListParserTest {

    private val parser = DscDataParser()

    @Test
    fun `correct data should be parsed`() {
        shouldNotThrowAny {
            val dscList = parser.parse(DSC_LIST_BASE64.decodeBase64()!!.toByteArray())
            dscList.dscList.size shouldBe 11
            dscList.dscList[0].kid shouldBe "6LVeJLKcq3s="
            dscList.dscList[9].kid shouldBe "hAeovR4V0yA="
        }
    }

    @Test
    fun `incorrect data not be parsed`() {
        shouldThrowAny {
            parser.parse("ABC123".toByteArray())
        }
    }
}
