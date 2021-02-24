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
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class SurveysTest {

    @MockK lateinit var deviceAttestation: DeviceAttestation
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var surveyServer: SurveyServer
    @MockK lateinit var oneTimePasswordRepo: OTPRepository
    @MockK lateinit var urlProvider: SurveyUrlProvider

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = Surveys(
        deviceAttestation,
        appConfigProvider,
        surveyServer,
        oneTimePasswordRepo,
        TestDispatcherProvider(),
        urlProvider,
        TimeStamper()
    )

    @Test
    fun `isConsentNeeded() should return Needed when no otp was yet authorized`() = runBlockingTest {
        every { oneTimePasswordRepo.otpAuthorizationResult } returns null
        createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) shouldBe Needed
    }

    @Test
    fun `isConsentNeeded() should return Needed when authentication of stored otp failed `() = runBlockingTest {
        every { oneTimePasswordRepo.otpAuthorizationResult } returns OTPAuthorizationResult(
            UUID.randomUUID(),
            authorized = false,
            redeemedAt = Instant.now(),
            invalidated = false
        )
        createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) shouldBe Needed
    }

    @Test
    fun `isConsentNeeded() should return Needed when an authorized otp was invalidated due to a risk change from high to low risk`() =
        runBlockingTest {
            every { oneTimePasswordRepo.otpAuthorizationResult } returns OTPAuthorizationResult(
                UUID.randomUUID(),
                authorized = true,
                redeemedAt = Instant.now(),
                invalidated = true
            )
            createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) shouldBe Needed
        }

    @Test
    fun `isConsentNeeded() should return AlreadyGiven when an authorized otp is stored and not invalidated`() =
        runBlockingTest {
            every { oneTimePasswordRepo.otpAuthorizationResult } returns OTPAuthorizationResult(
                UUID.randomUUID(),
                authorized = true,
                redeemedAt = Instant.now(),
                invalidated = false
            )
            coEvery { urlProvider.provideUrl(any(), any()) } returns ""
            createInstance().isConsentNeeded(HIGH_RISK_ENCOUNTER) should beInstanceOf<AlreadyGiven>()
        }
}