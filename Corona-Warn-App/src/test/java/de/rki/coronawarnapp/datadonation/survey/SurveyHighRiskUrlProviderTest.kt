package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SurveyHighRiskUrlProviderTest {

    @MockK lateinit var otpRepo: OTPRepository
    @MockK lateinit var appConfigProvider: AppConfigProvider

    @Test
    fun `provideUrl() should provide correct Url`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        every { otpRepo.lastOTP } returns OneTimePassword(uuid)
        coEvery { appConfigProvider.getAppConfig().survey.surveyOnHighRiskUrl } returns "http://www.example.com".toHttpUrl()

        createInstance().provideUrl(Surveys.Type.HIGH_RISK_ENCOUNTER) shouldBe "http://www.example.com/?queryParamNameOtp=$uuid"
    }

    @Test
    fun `provideUrl() should throw IllegalStateException when otp cannot be loaded`() = runBlockingTest {
        every { otpRepo.lastOTP } returns null
        coEvery { appConfigProvider.getAppConfig().survey.surveyOnHighRiskUrl } returns "http://www.example.com".toHttpUrl()

        shouldThrow<IllegalStateException> { createInstance().provideUrl(Surveys.Type.HIGH_RISK_ENCOUNTER) }.also {
            it.message shouldBe "Could not load OTP uuid from OTPRepository"
        }
    }

    @Test
    fun `provideUrl() should throw IllegalStateException when url from AppConfig is malformed`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        every { otpRepo.lastOTP } returns OneTimePassword(uuid)
        coEvery { appConfigProvider.getAppConfig().survey.surveyOnHighRiskUrl } returns null

        shouldThrow<IllegalStateException> { createInstance().provideUrl(Surveys.Type.HIGH_RISK_ENCOUNTER) }.also {
            it.message shouldBe "AppConfig doesn't contain a link to the high-risk card survey"
        }
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { appConfigProvider.getAppConfig().survey.otpQueryParameterName } returns "queryParamNameOtp"
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance() = SurveyUrlProvider(
        appConfigProvider,
        otpRepo
    )
}
