package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.OneTimePassword
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import java.util.UUID

class SurveySettingsTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var surveySettings: SurveySettings
    val preferences = MockSharedPreferences()
    val gson = Gson()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `load`() {
        val uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a1")
        val time = Instant.now()

        val instance = SurveySettings(context, gson)
        instance.oneTimePassword shouldBe null

        preferences.edit().putString("one_time_password", gson.toJson(OneTimePassword(uuid, time))).apply()

        val value = instance.oneTimePassword
        value shouldNotBe null
        value!!.uuid shouldBe uuid
        value.time shouldBe time
    }

    @Test
    fun `store`() {
        val uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
        val time = Instant.now()

        val instance = SurveySettings(context, gson)
        instance.oneTimePassword = OneTimePassword(uuid, time)

        val value = preferences.getString("one_time_password", null)
        value shouldNotBe null
        val fromJson = gson.fromJson(value, OneTimePassword::class.java)
        fromJson.uuid shouldBe uuid
        fromJson.time shouldBe time
    }
}
