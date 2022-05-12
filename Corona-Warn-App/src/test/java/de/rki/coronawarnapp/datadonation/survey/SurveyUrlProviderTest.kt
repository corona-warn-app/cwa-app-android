package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SurveyUrlProviderTest {

    @MockK lateinit var appConfigProvider: AppConfigProvider

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { appConfigProvider.getAppConfig().survey.otpQueryParameterName } returns "queryParamNameOtp"
    }

    private fun createInstance() = SurveyUrlProvider(
        appConfigProvider
    )

    @Test
    fun `provideUrl() should provide correct Url`() = runTest {
        val otp = UUID.randomUUID()
        coEvery { appConfigProvider.getAppConfig().survey.surveyOnHighRiskUrl } returns
            "http://www.example.com".toHttpUrl()

        createInstance().provideUrl(Surveys.Type.HIGH_RISK_ENCOUNTER, otp) shouldBe
            "http://www.example.com/?queryParamNameOtp=$otp"
    }

    @Test
    fun `provideUrl() should throw IllegalStateException when url from AppConfig is null`() = runTest {
        val otp = UUID.randomUUID()
        coEvery { appConfigProvider.getAppConfig().survey.surveyOnHighRiskUrl } returns null

        shouldThrow<IllegalStateException> { createInstance().provideUrl(Surveys.Type.HIGH_RISK_ENCOUNTER, otp) }.also {
            it.message shouldBe "AppConfig doesn't contain a link to the high-risk card survey"
        }
    }
}
