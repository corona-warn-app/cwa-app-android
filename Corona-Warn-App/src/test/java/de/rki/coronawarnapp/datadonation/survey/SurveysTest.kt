package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import de.rki.coronawarnapp.datadonation.survey.Surveys.ConsentResult.AlreadyGiven
import de.rki.coronawarnapp.datadonation.survey.Surveys.ConsentResult.Needed
import de.rki.coronawarnapp.datadonation.survey.Surveys.Type.HIGH_RISK_ENCOUNTER
import de.rki.coronawarnapp.datadonation.survey.server.SurveyServer
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.util.UUID

internal class SurveysTest : BaseTest() {

    @MockK lateinit var deviceAttestation: DeviceAttestation
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var surveyServer: SurveyServer
    @MockK lateinit var oneTimePasswordRepo: OTPRepository
    @MockK lateinit var urlProvider: SurveyUrlProvider
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-01T00:00:00.000Z")
    }

    private fun createInstance() = Surveys(
        deviceAttestation,
        appConfigProvider,
        surveyServer,
        oneTimePasswordRepo,
        TestDispatcherProvider(),
        urlProvider,
        timeStamper
    )

    @Test
    fun `isConsentNeeded() should return Needed when no otp was yet authorized`() = runTest {
        every { oneTimePasswordRepo.getOtpAuthorizationResult() } returns flowOf(null)
        createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) shouldBe Needed
    }

    @Test
    fun `isConsentNeeded() should return Needed when authentication of stored otp failed `() = runTest {
        every { oneTimePasswordRepo.getOtpAuthorizationResult() } returns flowOf(
            OTPAuthorizationResult(
                UUID.randomUUID(),
                authorized = false,
                redeemedAt = timeStamper.nowUTC,
                invalidated = false
            )
        )
        createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) shouldBe Needed
    }

    @Test
    fun `isConsentNeeded() returns Needed when an auth-otp was invalidated due to a risk change from high to low`() =
        runTest {
            every { oneTimePasswordRepo.getOtpAuthorizationResult() } returns flowOf(
                OTPAuthorizationResult(
                    UUID.randomUUID(),
                    authorized = true,
                    redeemedAt = timeStamper.nowUTC,
                    invalidated = true
                )
            )
            createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) shouldBe Needed
        }

    @Test
    fun `isConsentNeeded() should return AlreadyGiven when an authorized otp is stored and not invalidated`() =
        runTest {
            every { oneTimePasswordRepo.getOtpAuthorizationResult() } returns flowOf(
                OTPAuthorizationResult(
                    UUID.randomUUID(),
                    authorized = true,
                    redeemedAt = timeStamper.nowUTC,
                    invalidated = false
                )
            )
            coEvery { urlProvider.provideUrl(any(), any()) } returns ""
            createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) should beInstanceOf<AlreadyGiven>()
        }
}
