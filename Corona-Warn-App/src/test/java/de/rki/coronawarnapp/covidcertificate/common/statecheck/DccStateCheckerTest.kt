package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
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
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var mockDscData: DscData
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator
    @MockK lateinit var expirationChecker: DccExpirationChecker
    @MockK lateinit var mockData: DccData<*>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { configData.covidCertificateParameters } returns covidCertificateConfig
        every { covidCertificateConfig.expirationThresholdInDays } returns Duration.standardDays(10)
        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)

        every { dscRepository.dscData } returns flowOf(mockDscData)

        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1234567890)

        coEvery { dscSignatureValidator.isSignatureValid(any(), any()) } returns true
    }

    fun createInstance() = DccStateChecker(
        timeStamper = timeStamper,
        appConfigProvider = appConfigProvider,
        dscRepository = dscRepository,
        dscSignatureValidator = dscSignatureValidator,
        expirationChecker = expirationChecker,
    )

    @Test
    fun `state is valid`() = runBlockingTest {
        val state = CwaCovidCertificate.State.Valid(expiresAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().checkState(mockData).first() shouldBe state

        coVerify {
            dscSignatureValidator.isSignatureValid(mockDscData, mockData)
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

        createInstance().checkState(mockData).first() shouldBe state

        coVerify { dscSignatureValidator.isSignatureValid(mockDscData, mockData) }
    }

    @Test
    fun `state is expired`() = runBlockingTest {
        val state = CwaCovidCertificate.State.Expired(expiredAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().checkState(mockData).first() shouldBe state

        coVerify { dscSignatureValidator.isSignatureValid(mockDscData, mockData) }
    }

    @Test
    fun `invalid signature and expires soon`() = runBlockingTest {
        coEvery { dscSignatureValidator.isSignatureValid(any(), any()) } returns false
        val state = CwaCovidCertificate.State.ExpiringSoon(expiresAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().checkState(mockData).first() shouldBe CwaCovidCertificate.State.Invalid

        coVerify { dscSignatureValidator.isSignatureValid(mockDscData, mockData) }
    }

    @Test
    fun `invalid signature and expired`() = runBlockingTest {
        coEvery { dscSignatureValidator.isSignatureValid(any(), any()) } returns false
        val state = CwaCovidCertificate.State.Expired(expiredAt = Instant.EPOCH)
        coEvery { expirationChecker.getExpirationState(any(), any(), any()) } returns state

        createInstance().checkState(mockData).first() shouldBe CwaCovidCertificate.State.Invalid

        coVerify { dscSignatureValidator.isSignatureValid(mockDscData, mockData) }
    }
}
