package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.storage.LocalData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RegistrationTokenCensorTest : BaseTest() {

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.registrationToken() } returns testToken
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = RegistrationTokenCensor()

    @Test
    fun `censoring replaces the logline message`() {
        val instance = createInstance()
        val filterMe = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy registration token: $testToken",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(filterMe) shouldBe filterMe.copy(
            message = "I'm a shy registration token: 63b4###-####-####-####-############"
        )

        verify { LocalData.registrationToken() }
    }

    @Test
    fun `censoring returns null if thereis no match`() {
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
