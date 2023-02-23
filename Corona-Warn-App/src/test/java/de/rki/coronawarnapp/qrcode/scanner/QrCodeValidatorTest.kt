package de.rki.coronawarnapp.qrcode.scanner

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.PcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode3
import de.rki.coronawarnapp.coronatest.qrcode.raPcrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode3
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidPcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.di.DiTestProvider
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class QrCodeValidatorTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider

    private lateinit var qrCodeValidator: QrCodeValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { appConfigProvider.currentConfig } returns flowOf(
            mockk<ConfigData>().apply {
                every { presenceTracing } returns PresenceTracingConfigContainer(
                    qrCodeDescriptors = listOf(
                        PresenceTracingQRCodeDescriptor.newBuilder()
                            .setVersionGroupIndex(0)
                            .setEncodedPayloadGroupIndex(1)
                            .setPayloadEncoding(PresenceTracingQRCodeDescriptor.PayloadEncoding.BASE64)
                            .setRegexPattern("https://e\\.coronawarn\\.app\\?v=(\\d+)\\#(.+)")
                            .build()
                    )
                )
            }
        )

        qrCodeValidator = QrCodeValidator(
            dccQrCodeExtractor = DiTestProvider.extractor,
            raExtractor = RapidAntigenQrCodeExtractor(),
            pcrExtractor = PcrQrCodeExtractor(),
            checkInQrCodeExtractor = CheckInQrCodeExtractor(appConfigProvider),
            dccTicketingQrCodeExtractor = DiTestProvider.dccTicketingQrCodeExtractor,
            rapidPcrQrCodeExtractor = RapidPcrQrCodeExtractor()
        )
    }

    @Test
    fun `validator uses recognises DccQrCode`() = runTest {
        qrCodeValidator.validate(VaccinationTestData.personAVac1QRCodeString).apply {
            this as DccQrCode
            hash shouldBe VaccinationTestData.personAVac1StoredCertificateData.vaccinationQrCode.toSHA256()
        }
    }

    @Test
    fun `validator uses recognises CheckInQrCode`() = runTest {
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
    fun `validator uses recognises PCR QrCode`() = runTest {
        qrCodeValidator.validate(pcrQrCode1) should beInstanceOf(CoronaTestQRCode.PCR::class)
    }

    @Test
    fun `validator uses recognises RAT QrCode`() = runTest {
        qrCodeValidator.validate(raQrCode3).also {
            it should beInstanceOf(CoronaTestQRCode.Rapid::class)
            it should beInstanceOf(CoronaTestQRCode.RapidAntigen::class)
        }
    }

    @Test
    fun `validator uses recognises Rapid PCR QrCode`() = runTest {
        qrCodeValidator.validate(raPcrCode1).also {
            it should beInstanceOf(CoronaTestQRCode.Rapid::class)
            it should beInstanceOf(CoronaTestQRCode.RapidPCR::class)
        }
    }

    @Test
    fun `validator throws unsupported Error`() = runTest {
        shouldThrow<UnsupportedQrCodeException> {
            qrCodeValidator.validate("some text")
        }.errorCode shouldBe UnsupportedQrCodeException.ErrorCode.UNSUPPORTED_QR_CODE
    }

    @Test
    fun `valid codes are extracted by corresponding extractor`() = runTest {
        (qrCodeValidator.validate(pcrQrCode1) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.PCR
        (qrCodeValidator.validate(pcrQrCode2) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.PCR
        (qrCodeValidator.validate(pcrQrCode3) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.PCR

        (qrCodeValidator.validate(raQrCode1) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        (qrCodeValidator.validate(raQrCode2) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        (qrCodeValidator.validate(raQrCode3) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN

        (qrCodeValidator.validate(raPcrCode1) as CoronaTestQRCode).type shouldBe BaseCoronaTest.Type.PCR
    }
}
