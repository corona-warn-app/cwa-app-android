package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
class OrganizerRegistrationTokenCensorTest {

    private val testRegistrationTokens = listOf(
        "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f",
        "13b4d3ff-e0de-4bd4-90c1-17c2bb683a2x",
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        runBlocking { OrganizerRegistrationTokenCensor.clearRegistrationTokens() }
    }

    private fun createInstance() = OrganizerRegistrationTokenCensor()

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        val instance = createInstance()

        testRegistrationTokens.forEach {
            OrganizerRegistrationTokenCensor.addRegistrationToken(it)
            val toCensor = "I'm a shy registrationToken: $it"
            instance.checkLog(toCensor)!!
                .compile()!!.censored shouldBe "I'm a shy registrationToken: ########-####-####-####-########${it.takeLast(3)}"
        }
    }

    @Test
    fun `censoring aborts if no qrcode was set`() = runBlockingTest {
        val instance = createInstance()

        testRegistrationTokens.forEach {
            val toCensor = "I'm a shy registrationToken: $it"
            instance.checkLog(toCensor) shouldBe null
        }
    }
}
