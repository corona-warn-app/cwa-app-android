package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference
import java.util.UUID

class OTPRepositoryTest : BaseTest() {

    @MockK lateinit var surveySettings: SurveySettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `last otp is read from preferences`() {
        every { surveySettings.oneTimePassword } returns mockFlowPreference(
            OneTimePassword(UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0"))
        )
        OTPRepository(surveySettings).lastOTP!!.uuid shouldBe UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
    }

    @Test
    fun `no last otp`() {
        every { surveySettings.oneTimePassword } returns mockFlowPreference(null)
        OTPRepository(surveySettings).lastOTP shouldBe null
    }
}
