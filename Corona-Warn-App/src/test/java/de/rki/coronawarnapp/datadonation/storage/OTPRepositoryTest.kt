package de.rki.coronawarnapp.datadonation.storage

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import java.util.UUID

class OTPRepositoryTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var surveySettings: SurveySettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `last otp is read from preferences`() {
        val uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
        val time = Instant.ofEpochMilli(1612381131014)
        every { surveySettings.oneTimePassword } returns OneTimePassword(uuid, time)
        val lastOTP = OTPRepository(surveySettings).otp
        lastOTP shouldNotBe null
        lastOTP!!.apply {
            uuid shouldBe uuid
            time.toEpochMilli() shouldBe 1612381131014
        }
    }

    @Test
    fun `otp is stored upon creation`() {
        every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns MockSharedPreferences()
        val settings = SurveySettings(context, Gson())
        settings.oneTimePassword shouldBe null
        val generated = OTPRepository(settings).generateOTP()
        generated shouldBe settings.oneTimePassword
    }

    @Test
    fun `no last otp`() {
        every { surveySettings.oneTimePassword } returns null
        OTPRepository(surveySettings).otp shouldBe null
    }

    @Test
    fun `no otp auth result after generating new otp`() {
        every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns MockSharedPreferences()
        val settings = SurveySettings(context, Gson())
        settings.otpAuthorizationResult = OTPAuthorizationResult(
            UUID.randomUUID(),
            true,
            Instant.now()
        )

        settings.otpAuthorizationResult shouldNotBe null
        OTPRepository(settings).generateOTP()
        settings.otpAuthorizationResult shouldBe null
    }

    @Test
    fun `no otp after storing otp auth result`() {
        every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns MockSharedPreferences()
        val settings = SurveySettings(context, Gson())
        settings.oneTimePassword = OneTimePassword(UUID.randomUUID(), Instant.now())

        settings.oneTimePassword shouldNotBe null
        OTPRepository(settings).otpAuthorizationResult = OTPAuthorizationResult(
            UUID.randomUUID(),
            true,
            Instant.now()
        )
        settings.oneTimePassword shouldBe null
    }
}
