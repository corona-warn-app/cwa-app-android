package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import android.content.res.AssetManager
import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.cryptography.AesCryptography
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccHeaderParser
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.test.TestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestCertificateQRCodeExtractorTest : BaseTest() {
    private val schemaValidator by lazy {
        DccJsonSchemaValidator(
            DccJsonSchema(
                mockk<AssetManager>().apply {
                    every { open(any()) } answers { this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0)) }
                }
            ),
            JsonSchemaValidator()
        )
    }

    private val coseDecoder = DccCoseDecoder(AesCryptography())
    private val headerParser = DccHeaderParser()
    private val bodyParser = DccV1Parser(Gson(), schemaValidator)
    private val extractor = DccQrCodeExtractor(coseDecoder, headerParser, bodyParser)

    @Test
    fun `happy path qr code`() {
        val qrCode = extractor.extract(TestData.qrCodeTestCertificate) as TestCertificateQRCode
        with(qrCode.data.header) {
            issuer shouldBe "AT"
            issuedAt shouldBe Instant.parse("2021-06-01T10:12:48.000Z")
            expiresAt shouldBe Instant.parse("2021-06-03T10:12:48.000Z")
        }

        with(qrCode.data.certificate) {
            with(nameData) {
                familyName shouldBe "Musterfrau-Gößinger"
                familyNameStandardized shouldBe "MUSTERFRAU<GOESSINGER"
                givenName shouldBe "Gabriele"
                givenNameStandardized shouldBe "GABRIELE"
            }
            dateOfBirthFormatted shouldBe "1998-02-26"
            version shouldBe "1.2.1"

            with(test) {
                uniqueCertificateIdentifier shouldBe "URN:UVCI:01:AT:71EE2559DE38C6BF7304FB65A1A451EC#3"
                certificateCountry shouldBe "AT"
                certificateIssuer shouldBe "Ministry of Health, Austria"
                targetId shouldBe "840539006"
                sampleCollectedAt shouldBe Instant.parse("2021-02-20T12:34:56+00:00")
                testType shouldBe "LP217198-3"
                testCenter shouldBe "Testing center Vienna 1"
                testNameAndManufacturer shouldBe "1232"
                testResult shouldBe "260415000"
            }
        }
    }

    @Test
    fun `happy path cose decryption with Ellen Cheng`() {
        with(TestData.EllenCheng()) {
            val coseObject = coseWithEncryptedPayload.decodeBase64()!!.toByteArray()
            val dek = dek.decodeBase64()!!.toByteArray()
            val result = extractor.extractEncrypted(dek, coseObject)
            with(result.data.certificate.nameData) {
                familyName shouldBe "Cheng"
                givenName shouldBe "Ellen"
            }
            val result2 = extractor.extract(result.qrCode)
            with(result2.data.certificate.nameData) {
                familyName shouldBe "Cheng"
                givenName shouldBe "Ellen"
            }
        }
    }

    @Test
    fun `happy path cose decryption with Brian Calamandrei`() {
        with(TestData.BrianCalamandrei()) {
            val coseObject = coseWithEncryptedPayload.decodeBase64()!!.toByteArray()
            val dek = dek.decodeBase64()!!.toByteArray()
            val result = extractor.extractEncrypted(dek, coseObject)
            with(result.data.certificate.nameData) {
                familyName shouldBe "Calamandrei"
                givenName shouldBe "Brian"
            }
            val result2 = extractor.extract(result.qrCode)
            with(result2.data.certificate.nameData) {
                familyName shouldBe "Calamandrei"
                givenName shouldBe "Brian"
            }
        }
    }

    @Test
    fun `valid encoding but not a health certificate fails with HC_CWT_NO_ISS`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(VaccinationQrCodeTestData.validEncoded)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_ISS
    }

    @Test
    fun `random string fails with HC_BASE45_DECODING_FAILED`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract("nothing here to see")
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
    }

    @Test
    fun `uncompressed base45 string fails with HC_ZLIB_DECOMPRESSION_FAILED`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract("6BFOABCDEFGHIJKLMNOPQRSTUVWXYZ %*+-./:")
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
    }

    @Test
    fun `vaccination certificate fails with NO_TEST_ENTRY`() {
        shouldThrow<InvalidTestCertificateException> {
            extractor.extract(VaccinationQrCodeTestData.certificateMissing, mode = DccV1Parser.Mode.CERT_TEST_STRICT)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.NO_TEST_ENTRY
    }

    @Test
    fun `null values fail with JSON_SCHEMA_INVALID`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(TestData.qrCodeMssingValues)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_JSON_SCHEMA_INVALID
    }
}
