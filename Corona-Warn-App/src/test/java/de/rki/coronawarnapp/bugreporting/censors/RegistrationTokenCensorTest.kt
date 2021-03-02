package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class RegistrationTokenCensorTest : BaseTest() {
    @MockK lateinit var submissionSettings: SubmissionSettings

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

    private val regtokenPreference = mockFlowPreference<String?>(testToken)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false

        every { submissionSettings.registrationToken } returns regtokenPreference
    }

    private fun createInstance() = RegistrationTokenCensor(
        submissionSettings = submissionSettings
    )

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        val instance = createInstance()
        val filterMe = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy registration token: $testToken",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(filterMe) shouldBe filterMe.copy(
            message = "I'm a shy registration token: ########-####-####-####-########3a2f"
        )

        every { CWADebug.isDeviceForTestersBuild } returns true
        instance.checkLog(filterMe) shouldBe filterMe.copy(
            message = "I'm a shy registration token: 63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"
        )

        verify { regtokenPreference.value }
    }

    @Test
    fun `censoring returns null if there is no token`() = runBlockingTest {
        every { submissionSettings.registrationToken } returns mockFlowPreference(null)
        val instance = createInstance()
        val filterMeNot = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy registration token: $testToken",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring returns null if there is no match`() = runBlockingTest {
        val instance = createInstance()
        val filterMeNot = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm not a registration token ;)",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(filterMeNot) shouldBe null
    }
}
