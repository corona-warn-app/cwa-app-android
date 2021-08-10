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
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.util.serialization.SerializationModule
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
            JsonSchemaValidator(SerializationModule().jacksonObjectMapper())
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
    fun `required values that are null fail with JSON_SCHEMA_INVALID`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(TestData.qrCodeMissingRequiredValues)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_JSON_SCHEMA_INVALID
    }

    @Test
    fun `not required values that are null pass schema validation`() {
        val qrCode = extractor.extract(TestData.qrCodeMissingNotRequiredValues) as TestCertificateQRCode
        with(qrCode.data.header) {
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-08-10T09:02:14.000Z")
            expiresAt shouldBe Instant.parse("2022-08-10T09:02:14.000Z")
        }

        with(qrCode.data.certificate) {
            with(nameData) {
                familyName shouldBe "Darbiņš"
                familyNameStandardized shouldBe "DARBINS"
                givenName shouldBe "Aldis"
                givenNameStandardized shouldBe "ALDIS"
            }
            dateOfBirthFormatted shouldBe "1966-10-25"
            version shouldBe "1.0.0"

            with(test) {
                uniqueCertificateIdentifier shouldBe "urn:uvci:01:lv:e047f5373a6ea7794a7edd34eb204a12"
                certificateCountry shouldBe "LV"
                certificateIssuer shouldBe "Nacionālais veselības dienests"
                targetId shouldBe "840539006"
                sampleCollectedAt shouldBe Instant.parse("2021-07-01T09:44:52Z")
                testType shouldBe "LP6464-4"
                testCenter shouldBe "CENTRĀLĀ LABORATORIJA, SIA"
                testName shouldBe null
                testNameAndManufacturer shouldBe null
                testResult shouldBe "260415000"
            }
        }
    }

    @Test
    fun `whitespaces in JSON attributes are trimmed and pass schema validation`() {
        val qrCode = extractor.extract(TestData.qrCodeWhiteSpacesInAttributes) as VaccinationCertificateQRCode
        with(qrCode.data.header) {
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-08-10T09:02:26.000Z")
            expiresAt shouldBe Instant.parse("2022-08-10T09:02:26.000Z")
        }

        with(qrCode.data.certificate) {
            with(nameData) {
                familyName shouldBe "Mustermann"
                familyNameStandardized shouldBe "MUSTERMANN"
                givenName shouldBe "Erika"
                givenNameStandardized shouldBe "ERIKA"
            }
            dateOfBirthFormatted shouldBe "1964-08-12"
            version shouldBe "1.3.0"

            with(vaccination) {
                uniqueCertificateIdentifier shouldBe "URN:UVCI:01DE/IZ12345A/5CWLU12RNOB9RXSEOP6FG8#W"
                certificateCountry shouldBe "DE"
                certificateIssuer shouldBe "Robert Koch-Institut"
                targetId shouldBe "840539006"
                marketAuthorizationHolderId shouldBe "ORG-100031184"
                medicalProductId shouldBe "EU/1/20/1507"
            }
        }
    }
}
