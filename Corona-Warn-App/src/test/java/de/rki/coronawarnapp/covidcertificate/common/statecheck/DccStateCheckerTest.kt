package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationChecker
import de.rki.coronawarnapp.covidcertificate.revocation.check.DccRevocationChecker
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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant

class DccStateCheckerTest : BaseTest() {
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var covidCertificateConfig: CovidCertificateConfig
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var mockDscData: DscSignatureList
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator
    @MockK lateinit var dccExpirationChecker: DccExpirationChecker
    @MockK lateinit var dccRevocationChecker: DccRevocationChecker
    @MockK lateinit var mockData: DccData<*>

    private lateinit var dccValidityMeasures: DccValidityMeasures

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { configData.covidCertificateParameters } returns covidCertificateConfig
        every { covidCertificateConfig.expirationThreshold } returns Duration.ofDays(10)
        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1234567890)

        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } just Runs
        coEvery { dccRevocationChecker.isRevoked(any(), any()) } returns false

        dccValidityMeasures = DccValidityMeasures(
            dscSignatureList = mockDscData,
            blockedQrCodeHashes = setOf(),
            revocationList = listOf()
        )
    }

    fun createInstance() = DccStateChecker(
        timeStamper = timeStamper,
        appConfigProvider = appConfigProvider,
        dscSignatureValidator = dscSignatureValidator,
        dccExpirationChecker = dccExpirationChecker,
        dccRevocationChecker = dccRevocationChecker
    )

    @Test
    fun `state is valid`() = runTest {
        val state = CwaCovidCertificate.State.Valid(expiresAt = Instant.EPOCH)
        coEvery { dccExpirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccValidityMeasures) shouldBe state

        coVerify {
            dscSignatureValidator.validateSignature(mockData, mockDscData, any())
            dccExpirationChecker.getExpirationState(
                dccData = mockData,
                expirationThreshold = Duration.ofDays(10),
                now = Instant.ofEpochSecond(1234567890)
            )
        }
    }

    @Test
    fun `state is expiring soon`() = runTest {
        val state = CwaCovidCertificate.State.ExpiringSoon(expiresAt = Instant.EPOCH)
        coEvery { dccExpirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccValidityMeasures) shouldBe state

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `state is expired`() = runTest {
        val state = CwaCovidCertificate.State.Expired(expiredAt = Instant.EPOCH)
        coEvery { dccExpirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccValidityMeasures) shouldBe state

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `invalid signature and expires soon`() = runTest {
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } throws Exception()
        val state = CwaCovidCertificate.State.ExpiringSoon(expiresAt = Instant.EPOCH)
        coEvery { dccExpirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccValidityMeasures) shouldBe CwaCovidCertificate.State.Invalid()

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `invalid signature and expired`() = runTest {
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } throws Exception()
        val state = CwaCovidCertificate.State.Expired(expiredAt = Instant.EPOCH)
        coEvery { dccExpirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().invoke(mockData, "", dccValidityMeasures) shouldBe CwaCovidCertificate.State.Invalid()

        coVerify { dscSignatureValidator.validateSignature(mockData, mockDscData, any()) }
    }

    @Test
    fun `state is blocked`() = runTest {
        createInstance().invoke(
            dccData = mockData,
            qrCodeHash = "qrCodeHash",
            dccValidityMeasures.copy(blockedQrCodeHashes = setOf("qrCodeHash"))
        ) shouldBe CwaCovidCertificate.State.Blocked

        coVerify {
            dscSignatureValidator wasNot Called
            dccExpirationChecker wasNot Called
            timeStamper wasNot Called
        }
    }

    @Test
    fun `state is revoked`() = runTest {
        coEvery { dccRevocationChecker.isRevoked(any(), any()) } returns true
        createInstance().invoke(
            dccData = mockData,
            qrCodeHash = "qrCodeHash",
            dccValidityMeasures.copy(blockedQrCodeHashes = setOf("qrCodeHash"))
        ) shouldBe CwaCovidCertificate.State.Revoked

        coVerify {
            dscSignatureValidator wasNot Called
            dccExpirationChecker wasNot Called
            timeStamper wasNot Called
        }
    }
}
