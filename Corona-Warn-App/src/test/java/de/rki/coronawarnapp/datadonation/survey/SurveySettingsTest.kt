package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.serialization.SerializationModule
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

class SurveySettingsTest : BaseTest() {

    @MockK lateinit var context: Context
    val preferences = MockSharedPreferences()
    lateinit var baseGson: Gson

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        baseGson = SerializationModule().baseGson().newBuilder().apply {
            setPrettyPrinting()
        }.create()
        every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    @Test
    fun `load and deserialize otp json`() {
        val instance = SurveySettings(context, baseGson)
        instance.oneTimePassword shouldBe null

        preferences.edit().putString(
            "one_time_password",
            """
                {
                    "uuid":"e103c755-0975-4588-a639-d0cd1ba421a1",
                    "time": 1612381217442
                }
            """.trimIndent()
        ).apply()

        val value = instance.oneTimePassword
        value shouldNotBe null
        value!!.uuid.toString() shouldBe "e103c755-0975-4588-a639-d0cd1ba421a1"
        value.time.toEpochMilli() shouldBe 1612381217442
    }

    @Test
    fun `otp parsing error`() {
        val instance = SurveySettings(context, baseGson)
        instance.oneTimePassword shouldBe null

        preferences
            .edit()
            .putString("one_time_password", "invalid value")
            .apply()

        instance.oneTimePassword shouldBe null
    }

    @Test
    fun `save and serialize otp json`() {
        val uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
        val time = Instant.ofEpochMilli(1612381567242)

        val instance = SurveySettings(context, baseGson)
        instance.oneTimePassword = OneTimePassword(uuid, time)

        val value = preferences.getString("one_time_password", null)
        value shouldBe """
            {
              "uuid": "e103c755-0975-4588-a639-d0cd1ba421a0",
              "time": 1612381567242
            }
        """.trimIndent()
    }

    @Test
    fun `load and deserialize auth result json`() {
        val instance = SurveySettings(context, baseGson)
        instance.otpAuthorizationResult shouldBe null

        preferences.edit().putString(
            "otp_result",
            """
                {
                    "uuid":"e103c755-0975-4588-a639-d0cd1ba421a1",
                    "authorized": true,
                    "redeemedAt": 1612381217443,
                    "invalidated": true
                }
            """.trimIndent()
        ).apply()

        val value = instance.otpAuthorizationResult
        value shouldNotBe null
        value!!.uuid.toString() shouldBe "e103c755-0975-4588-a639-d0cd1ba421a1"
        value.authorized shouldBe true
        value.redeemedAt.toEpochMilli() shouldBe 1612381217443
        value.invalidated shouldBe true
    }

    @Test
    fun `auth result parsing error`() {
        val instance = SurveySettings(context, baseGson)
        instance.otpAuthorizationResult shouldBe null

        preferences
            .edit()
            .putString("otp_result", "invalid value")
            .apply()

        instance.otpAuthorizationResult shouldBe null
    }

    @Test
    fun `save and serialize auth result json`() {
        val uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
        val authorized = false
        val redeemedAt = Instant.ofEpochMilli(1612381217445)

        val instance = SurveySettings(context, baseGson)
        instance.otpAuthorizationResult = OTPAuthorizationResult(uuid, authorized, redeemedAt, false)

        val value = preferences.getString("otp_result", null)
        value shouldBe """
            {
              "uuid": "e103c755-0975-4588-a639-d0cd1ba421a0",
              "authorized": false,
              "redeemedAt": 1612381217445,
              "invalidated": false
            }
        """.trimIndent()
    }
}
