package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.util.UUID

class OTPRepositoryTest : BaseTest() {

    @MockK lateinit var surveySettings: SurveySettings
    private val objectMapper = SerializationModule().jacksonObjectMapper()
    private val dataStore = FakeDataStore()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `last otp is read from preferences`() = runTest {
        val uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
        val time = Instant.ofEpochMilli(1612381131014)
        coEvery { surveySettings.oneTimePassword.first() } returns OneTimePassword(uuid, time)
        val lastOTP = OTPRepository(surveySettings).getOtp()
        lastOTP shouldNotBe null
        lastOTP!!.apply {
            uuid shouldBe uuid
            time.toEpochMilli() shouldBe 1612381131014
        }
    }

    @Test
    fun `otp is stored upon creation`() = runTest {
        // every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns MockSharedPreferences()
        val settings = SurveySettings(dataStore, objectMapper)
        settings.oneTimePassword shouldBe null
        val generated = OTPRepository(settings).generateOTP()
        generated shouldBe settings.oneTimePassword
    }

    @Test
    fun `no last otp`() = runTest {
        coEvery { surveySettings.oneTimePassword.first() } returns null
        OTPRepository(surveySettings).getOtp() shouldBe null
    }

    @Test
    fun `no otp auth result after generating new otp`() = runTest {
        // every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns MockSharedPreferences()
        val settings = SurveySettings(dataStore, objectMapper)
        settings.updateOtpAuthorizationResult(
            OTPAuthorizationResult(
                UUID.randomUUID(),
                true,
                Instant.now()
            )
        )

        settings.otpAuthorizationResult shouldNotBe null
        OTPRepository(settings).generateOTP()
        settings.otpAuthorizationResult shouldBe null
    }

    @Test
    fun `no otp after storing otp auth result`() = runTest {
        // every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns MockSharedPreferences()
        val settings = SurveySettings(dataStore, objectMapper)
        settings.updateOneTimePassword(OneTimePassword(UUID.randomUUID(), Instant.now()))

        settings.oneTimePassword shouldNotBe null
        OTPRepository(settings).updateOtpAuthorizationResult(
            OTPAuthorizationResult(
                UUID.randomUUID(),
                true,
                Instant.now()
            )
        )
        settings.oneTimePassword shouldBe null
    }
}
