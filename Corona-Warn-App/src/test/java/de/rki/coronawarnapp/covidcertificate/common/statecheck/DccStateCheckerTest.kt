package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccStateCheckerTest : BaseTest() {
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var covidCertificateConfig: CovidCertificateConfig
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator
    @MockK lateinit var expirationChecker: DccExpirationChecker

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { configData.covidCertificateParameters } returns covidCertificateConfig
        every { covidCertificateConfig.expirationThresholdInDays } returns Duration.standardDays(10)
        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)
    }

    fun createInstance() = DccStateChecker(
        timeStamper = timeStamper,
        appConfigProvider = appConfigProvider,
        dscRepository = dscRepository,
        dscSignatureValidator = dscSignatureValidator,
        expirationChecker = expirationChecker,
    )

    @Test
    fun `state is valid`() {
        TODO()
    }

    @Test
    fun `state is expiring soon`() {
        TODO()
    }

    @Test
    fun `state is expired`() {
        TODO()
    }

    @Test
    fun `invalid signature and expires soon`() {
        TODO()
    }

    @Test
    fun `invalid signature and expired`() {
        TODO()
    }
}
