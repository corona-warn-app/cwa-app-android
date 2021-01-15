package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RegistrationTokenCensorTest : BaseTest() {

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false

        mockkObject(LocalData)
        every { LocalData.registrationToken() } returns testToken
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = RegistrationTokenCensor()

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

        verify { LocalData.registrationToken() }
    }

    @Test
    fun `censoring returns null if there is no token`() = runBlockingTest {
        every { LocalData.registrationToken() } returns null
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
