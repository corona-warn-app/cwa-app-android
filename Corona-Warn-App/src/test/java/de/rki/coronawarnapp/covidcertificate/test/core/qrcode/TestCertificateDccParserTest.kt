package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import android.content.res.AssetManager
import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.test.TestData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import testhelpers.extensions.toInstant

class TestCertificateDccParserTest {

    private val schemaValidator by lazy {
        DccJsonSchemaValidator(
            DccJsonSchema(
                mockk<AssetManager>().apply {
                    every { open(any()) } answers { this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0)) }
                }
            ),
            JsonSchemaValidator(SerializationModule().jacksonObjectMapper())
        )
    }
    private val bodyParser = DccV1Parser(Gson(), schemaValidator)

    @Test
    fun `happy path cose decryption with Ellen Cheng`() {
        val coseObject = CBORObject.DecodeFromBytes(TestData.cborObject.decodeHex().toByteArray())
        val body = bodyParser.parse(coseObject, DccV1Parser.Mode.CERT_TEST_STRICT)
        with(body.parsed) {
            with(nameData) {
                familyName shouldBe "Musterfrau-Gößinger"
                familyNameStandardized shouldBe "MUSTERFRAU<GOESSINGER"
                givenName shouldBe "Gabriele"
                givenNameStandardized shouldBe "GABRIELE"
            }
            dob shouldBe "1998-02-26"
            dateOfBirthFormatted shouldBe "1998-02-26"
            version shouldBe "1.2.1"

            with(tests!!.single()) {
                uniqueCertificateIdentifier shouldBe "URN:UVCI:01:AT:71EE2559DE38C6BF7304FB65A1A451EC#3"
                certificateCountry shouldBe "AT"
                certificateIssuer shouldBe "Ministry of Health, Austria"
                targetId shouldBe "840539006"
                sampleCollectedAt shouldBe "2021-02-20T12:34:56+00:00".toInstant()
                testType shouldBe "LP217198-3"
                testCenter shouldBe "Testing center Vienna 1"
                testNameAndManufacturer shouldBe "1232"
                testResult shouldBe "260415000"
            }
        }
        with(body.raw) {
            this shouldContain "Musterfrau-Gößinger"
            this shouldContain "URN:UVCI:01:AT:71EE2559DE38C6BF7304FB65A1A451EC#3"
        }
    }
}
