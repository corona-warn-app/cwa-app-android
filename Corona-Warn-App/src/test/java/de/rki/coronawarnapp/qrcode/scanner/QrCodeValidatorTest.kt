package de.rki.coronawarnapp.qrcode.scanner

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode3
import de.rki.coronawarnapp.coronatest.qrcode.raPcrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode3
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

@Suppress("MaxLineLength")
class QrCodeValidatorTest : BaseTest() {
    @Inject
    lateinit var testData: VaccinationTestData

    @Inject
    lateinit var qrCodeValidator: QrCodeValidator

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `validator uses recognises DccQrCode`() = runBlockingTest {
        qrCodeValidator.validate(testData.personAVac1QRCodeString).apply {
            this as DccQrCode
            hash shouldBe testData.personAVac1Container.qrCodeHash
        }
    }

    @Test
    fun `validator uses recognises CheckInQrCode`() = runBlockingTest {
        val checkInUrl =
            "https://e.coronawarn.app?v=1#CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmoIAR" +
                "JgOMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-vox" +
                "Q1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGgQxMjM0IgQIARAC"

        qrCodeValidator.validate(checkInUrl).apply {
            this as CheckInQrCode
            this.qrCodePayload.locationData.address shouldBe "at my place"
        }
    }

    @Test
    fun `validator uses recognises PCR QrCode`() = runBlockingTest {
        qrCodeValidator.validate(pcrQrCode1).apply {
            this as CoronaTestQRCode
        }
    }

    @Test
    fun `validator uses recognises RAT QrCode`() = runBlockingTest {
        qrCodeValidator.validate(raQrCode3).apply {
            this as CoronaTestQRCode
        }
    }

    @Test
    fun `validator throws unsupported Error`() = runBlockingTest {
        shouldThrow<UnsupportedQrCodeException> {
            qrCodeValidator.validate("some text")
        }.errorCode shouldBe UnsupportedQrCodeException.ErrorCode.UNSUPPORTED_QR_CODE
    }

    @Test
    fun `valid codes are extracted by corresponding extractor`() = runBlockingTest {
        (qrCodeValidator.validate(pcrQrCode1) as CoronaTestQRCode).type shouldBe CoronaTest.Type.PCR
        (qrCodeValidator.validate(pcrQrCode2) as CoronaTestQRCode).type shouldBe CoronaTest.Type.PCR
        (qrCodeValidator.validate(pcrQrCode3) as CoronaTestQRCode).type shouldBe CoronaTest.Type.PCR

        (qrCodeValidator.validate(raQrCode1) as CoronaTestQRCode).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        (qrCodeValidator.validate(raQrCode2) as CoronaTestQRCode).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        (qrCodeValidator.validate(raQrCode3) as CoronaTestQRCode).type shouldBe CoronaTest.Type.RAPID_ANTIGEN

        (qrCodeValidator.validate(raPcrCode1) as CoronaTestQRCode).type shouldBe CoronaTest.Type.PCR
    }
}
