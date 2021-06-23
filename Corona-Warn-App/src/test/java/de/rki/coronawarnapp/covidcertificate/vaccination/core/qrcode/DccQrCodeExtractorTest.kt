package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_ISS
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NO_RECOVERY_ENTRY
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NO_TEST_ENTRY
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NO_VACCINATION_ENTRY
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidRecoveryCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccQrCodeExtractorTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor
    @Inject lateinit var vaccinationTestData: VaccinationTestData

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)
    }

    @Test
    fun `happy path extraction`() {
        extractor.extract(VaccinationQrCodeTestData.validVaccinationQrCode, mode = Mode.CERT_VAC_STRICT)
    }

    @Test
    fun `happy path extraction 2`() {
        extractor.extract(VaccinationQrCodeTestData.validVaccinationQrCode2)
    }

    @Test
    fun `happy path extraction with data`() {
        val qrCode = extractor.extract(
            VaccinationQrCodeTestData.validVaccinationQrCode3,
            mode = Mode.CERT_VAC_STRICT
        ) as VaccinationCertificateQRCode

        with(qrCode.data.header) {
            issuer shouldBe "AT"
            issuedAt shouldBe Instant.ofEpochSecond(1620392021)
            expiresAt shouldBe Instant.ofEpochSecond(1620564821)
        }

        with(qrCode.data.certificate) {
            with(nameData) {
                familyName shouldBe "Musterfrau-Gößinger"
                familyNameStandardized shouldBe "MUSTERFRAU<GOESSINGER"
                givenName shouldBe "Gabriele"
                givenNameStandardized shouldBe "GABRIELE"
            }
            dateOfBirthFormatted shouldBe "1998-02-26"
            version shouldBe "1.0.0"

            with(vaccination) {
                uniqueCertificateIdentifier shouldBe "urn:uvci:01:AT:10807843F94AEE0EE5093FBC254BD813P"
                certificateCountry shouldBe "AT"
                doseNumber shouldBe 1
                dt shouldBe "2021-02-18"
                certificateIssuer shouldBe "BMSGPK Austria"
                marketAuthorizationHolderId shouldBe "ORG-100030215"
                medicalProductId shouldBe "EU/1/20/1528"
                totalSeriesOfDoses shouldBe 2
                targetId shouldBe "840539006"
                vaccineId shouldBe "1119305005"
                vaccinatedOnFormatted shouldBe "2021-02-18"
            }
        }
    }

    @Test
    fun `happy path extraction 4`() {
        extractor.extract(
            VaccinationQrCodeTestData.validVaccinationQrCode4,
            mode = Mode.CERT_VAC_STRICT
        )
    }

    @Test
    fun `valid encoding but not a health certificate fails with HC_CWT_NO_ISS`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.validEncoded,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe HC_CWT_NO_ISS
    }

    @Test
    fun `random string fails with HC_BASE45_DECODING_FAILED`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                "nothing here to see",
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe HC_BASE45_DECODING_FAILED
    }

    @Test
    fun `uncompressed base45 string fails with HC_ZLIB_DECOMPRESSION_FAILED`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                "6BFOABCDEFGHIJKLMNOPQRSTUVWXYZ %*+-./:",
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe HC_ZLIB_DECOMPRESSION_FAILED
    }

    @Test
    fun `vaccination certificate missing fails with VC_NO_VACCINATION_ENTRY`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.certificateMissing,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe NO_VACCINATION_ENTRY
    }

    @Test
    fun `test data person A check`() {
        val extracted = extractor.extract(
            vaccinationTestData.personAVac1QRCodeString,
            mode = Mode.CERT_VAC_STRICT
        )
        extracted shouldBe vaccinationTestData.personAVac1QRCode
    }

    @Test
    fun `test data person B check`() {
        val extracted = extractor.extract(
            vaccinationTestData.personBVac1QRCodeString,
            mode = Mode.CERT_VAC_STRICT
        )
        extracted shouldBe vaccinationTestData.personBVac1QRCode
    }

    @Test
    fun `null values fail with JSON_SCHEMA_INVALID`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.qrCodeWithNullValues,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
    }

    @Test
    fun `blank name fails with JSON_SCHEMA_INVALID`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.qrCodeBlankLastNameStandardized,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
    }

    @Test
    fun `Bulgarian qr code passes`() {
        val qrCode = extractor.extract(
            VaccinationQrCodeTestData.qrCodeBulgaria,
            mode = Mode.CERT_VAC_STRICT
        ) as VaccinationCertificateQRCode
        with(qrCode.data.header) {
            issuer shouldBe "BG"
            issuedAt shouldBe Instant.parse("2021-06-02T14:07:56.000Z")
            expiresAt shouldBe Instant.parse("2022-06-02T14:07:56.000Z")
        }

        with(qrCode.data.certificate) {
            with(nameData) {
                familyName shouldBe "ПЕТКОВ"
                familyNameStandardized shouldBe "PETKOV"
                givenName shouldBe "СТАМО ГЕОРГИЕВ"
                givenNameStandardized shouldBe "STAMO<GEORGIEV"
            }
            dateOfBirthFormatted shouldBe "1978-01-26"
            version shouldBe "1.0.0"

            vaccination.apply {
                uniqueCertificateIdentifier shouldBe "urn:uvci:01:BG:UFR5PLGKU8WDSZK7#0"
                certificateCountry shouldBe "BG"
                doseNumber shouldBe 2
                dt shouldBe "2021-03-09T00:00:00"
                certificateIssuer shouldBe "Ministry of Health"
                marketAuthorizationHolderId shouldBe "ORG-100030215"
                medicalProductId shouldBe "EU/1/20/1528"
                totalSeriesOfDoses shouldBe 2
                targetId shouldBe "840539006"
                vaccineId shouldBe "J07BX03"
                vaccinatedOnFormatted shouldBe "2021-03-09"
            }
        }
    }

    @Test
    fun `Swedish qr code passes`() {
        extractor.extract(
            VaccinationQrCodeTestData.qrCodeSweden,
            mode = Mode.CERT_VAC_STRICT
        )
    }

    @Test
    fun `Polish qr code passes`() {
        extractor.extract(
            VaccinationQrCodeTestData.qrCodePoland,
            mode = Mode.CERT_VAC_STRICT
        )
    }

    @Test
    fun `fail vaccinated at date without day`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.failVaccinatedAtWithoutDay1,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
    }

    @Test
    fun `fail vaccinated at date without day and month`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.failVaccinatedAtWithoutDayAndMonth,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
    }

    @Test
    fun `pass german reference case`() {
        extractor.extract(
            VaccinationQrCodeTestData.passGermanReferenceCase,
            mode = Mode.CERT_VAC_STRICT
        ).apply {
            this as VaccinationCertificateQRCode
            data.certificate.dateOfBirthFormatted shouldBe "1964-08-12"
            data.certificate.vaccination.vaccinatedOnFormatted shouldBe "2021-05-29"
        }
    }

    @Test
    fun `pass vaccination and dob with time at midnight`() {
        extractor.extract(
            VaccinationQrCodeTestData.passDatesWithTimeAtMidnight,
            mode = Mode.CERT_VAC_STRICT
        ).apply {
            this as VaccinationCertificateQRCode
            data.certificate.dateOfBirthFormatted shouldBe "1978-01-26"
            data.certificate.vaccination.vaccinatedOnFormatted shouldBe "2021-03-09"
        }
    }

    @Test
    fun `pass vaccination date with full timestamp`() {
        extractor.extract(
            VaccinationQrCodeTestData.passDatesWithRealTimeInfo,
            mode = Mode.CERT_VAC_STRICT
        ).apply {
            this as VaccinationCertificateQRCode
            data.certificate.dateOfBirthFormatted shouldBe "1958-11-11"
            data.certificate.vaccination.vaccinatedOnFormatted shouldBe "2021-03-18"
        }
    }

    @Test
    fun `happy path extraction recovery`() {
        extractor.extract(
            RecoveryQrCodeTestData.validRecovery,
        )
    }

    @Test
    fun `happy path extraction recovery with strict mode`() {
        extractor.extract(
            RecoveryQrCodeTestData.validRecovery,
            mode = Mode.CERT_REC_STRICT
        )
    }

    @Test
    fun `recovery cert fails in mode CERT_VAC_STRICT`() {
        shouldThrow<InvalidVaccinationCertificateException> {
            extractor.extract(
                RecoveryQrCodeTestData.validRecovery,
                mode = Mode.CERT_VAC_STRICT
            )
        }.errorCode shouldBe NO_VACCINATION_ENTRY
    }

    @Test
    fun `recovery cert fails in mode CERT_TEST_STRICT`() {
        shouldThrow<InvalidTestCertificateException> {
            extractor.extract(
                RecoveryQrCodeTestData.validRecovery,
                mode = Mode.CERT_TEST_STRICT
            )
        }.errorCode shouldBe NO_TEST_ENTRY
    }

    @Test
    fun `vaccination cert fails in mode CERT_REC_STRICT`() {
        shouldThrow<InvalidRecoveryCertificateException> {
            extractor.extract(
                VaccinationQrCodeTestData.validVaccinationQrCode,
                mode = Mode.CERT_REC_STRICT
            )
        }.errorCode shouldBe NO_RECOVERY_ENTRY
    }
}
