package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
        every { surveySettings.oneTimePassword } returns flowOf(OneTimePassword(uuid, time))
        val lastOTP = OTPRepository(surveySettings).getOtp()
        lastOTP shouldNotBe null
        lastOTP!!.apply {
            uuid shouldBe uuid
            time.toEpochMilli() shouldBe 1612381131014
        }
    }

    @Test
    fun `otp is stored upon creation`() = runTest {
        val settings = SurveySettings(dataStore, objectMapper)
        settings.oneTimePassword.first() shouldBe null
        val generated = OTPRepository(settings).generateOTP()
        generated.uuid shouldBe settings.oneTimePassword.first()!!.uuid
        generated.time.epochSecond shouldBe settings.oneTimePassword.first()!!.time.epochSecond
    }

    @Test
    fun `no last otp`() = runTest {
        coEvery { surveySettings.oneTimePassword } returns flowOf(null)
        OTPRepository(surveySettings).getOtp() shouldBe null
    }

    @Test
    fun `no otp auth result after generating new otp`() = runTest {
        val settings = SurveySettings(dataStore, objectMapper)
        settings.updateOtpAuthorizationResult(
            OTPAuthorizationResult(
                UUID.randomUUID(),
                true,
                Instant.now()
            )
        )

        settings.otpAuthorizationResult.first() shouldNotBe null
        OTPRepository(settings).generateOTP()
        settings.otpAuthorizationResult.first() shouldBe null
    }

    @Test
    fun `no otp after storing otp auth result`() = runTest {
        val settings = SurveySettings(dataStore, objectMapper)
        settings.updateOneTimePassword(OneTimePassword(UUID.randomUUID(), Instant.now()))

        settings.oneTimePassword.first() shouldNotBe null
        OTPRepository(settings).updateOtpAuthorizationResult(
            OTPAuthorizationResult(
                UUID.randomUUID(),
                true,
                Instant.now()
            )
        )
        settings.oneTimePassword.first() shouldBe null
    }
}
