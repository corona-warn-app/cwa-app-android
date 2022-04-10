package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccStateCheckerTest : BaseTest() {
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var covidCertificateConfig: CovidCertificateConfig
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var mockDscData: DscSignatureList
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator
    @MockK lateinit var expirationChecker: DccExpirationChecker
    @MockK lateinit var mockData: DccData<*>

    private val dccStateValidity = DccStateValidity(
        dscSignatureList = mockDscData,
        blockedQrCodeHashes = setOf(),
        revocationList = listOf()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { configData.covidCertificateParameters } returns covidCertificateConfig
        every { covidCertificateConfig.expirationThreshold } returns Duration.standardDays(10)
        every { covidCertificateConfig.blockListParameters } returns emptyList()
        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)
        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1234567890)

        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } just Runs
    }

    fun createInstance() = DccStateChecker(
        timeStamper = timeStamper,
        appConfigProvider = appConfigProvider,
        dscSignatureValidator = dscSignatureValidator,
        expirationChecker = expirationChecker,
        revocationCheck =
    )

    @Test
    fun `state is valid`() = runBlockingTest {
        val state = CwaCovidCertificate.State.Valid(expiresAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccStateValidity) shouldBe state

        coVerify {
            dscSignatureValidator.validateSignature(mockData, mockDscData, any())
            expirationChecker.getExpirationState(
                dccData = mockData,
                expirationThreshold = Duration.standardDays(10),
                now = Instant.ofEpochSecond(1234567890)
            )
        }
    }

    @Test
    fun `state is expiring soon`() = runBlockingTest {
        val state = CwaCovidCertificate.State.ExpiringSoon(expiresAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccStateValidity) shouldBe state

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `state is expired`() = runBlockingTest {
        val state = CwaCovidCertificate.State.Expired(expiredAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccStateValidity) shouldBe state

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `invalid signature and expires soon`() = runBlockingTest {
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } throws Exception()
        val state = CwaCovidCertificate.State.ExpiringSoon(expiresAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccStateValidity) shouldBe CwaCovidCertificate.State.Invalid()

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `invalid signature and expired`() = runBlockingTest {
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } throws Exception()
        val state = CwaCovidCertificate.State.Expired(expiredAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccStateValidity) shouldBe CwaCovidCertificate.State.Invalid()

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `state is blocked`() = runBlockingTest {
        createInstance().invoke(
            dccData = mockData,
            qrCodeHash = "qrCodeHash",
            dccStateValidity.copy(blockedQrCodeHashes = setOf("qrCodeHash"))
        ) shouldBe CwaCovidCertificate.State.Blocked

        coVerify {
            dscSignatureValidator wasNot Called
            expirationChecker wasNot Called
            timeStamper wasNot Called
        }
    }
}
