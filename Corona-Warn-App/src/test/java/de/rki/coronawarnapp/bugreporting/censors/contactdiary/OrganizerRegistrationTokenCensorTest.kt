package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
@Disabled
class OrganizerRegistrationTokenCensorTest {

    private val tans = listOf(
        "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f",
        "13b4d3ff-e0de-4bd4-90c1-17c2bb683a2x",
    )

    private val testRegistrationRequests = listOf(
        RegistrationRequest("1234567890", VerificationKeyType.TELETAN),
        RegistrationRequest("9876543210", VerificationKeyType.TELETAN),
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() = runTest {
        OrganizerRegistrationTokenCensor.clearTan()
        OrganizerRegistrationTokenCensor.clearRegistrationRequests()
    }

    private fun createInstance() = OrganizerRegistrationTokenCensor()

    @Test
    fun `censoring replaces the logline message for tans`() = runTest {
        val instance = createInstance()

        tans.forEach {
            OrganizerRegistrationTokenCensor.addTan(it)
            val toCensor = "I'm a shy TAN: $it"
            instance.checkLog(toCensor)!!
                .compile()!!.censored shouldBe "I'm a shy TAN: ########-####-####-####-########${it.takeLast(3)}"
        }
    }

    @Test
    fun `censoring replaces the logline message for registration requests`() = runTest {
        val instance = createInstance()

        testRegistrationRequests.forEach {
            OrganizerRegistrationTokenCensor.addRegistrationRequestToCensor(it)
            val toCensor = "I'm a shy registration request key: ${it.key}"
            instance.checkLog(toCensor)!!
                .compile()!!.censored shouldBe "I'm a shy registration request key: ###-###-${it.key.takeLast(4)}"
        }
    }

    @Test
    fun `censoring aborts if no qrcode was set`() = runTest {
        val instance = createInstance()

        testRegistrationRequests.forEach {
            val toCensor = "I'm a shy registrationToken: $it"
            instance.checkLog(toCensor) shouldBe null
        }

        tans.forEach {
            val toCensor = "I'm a shy registrationToken: $it"
            instance.checkLog(toCensor) shouldBe null
        }
    }
}
