package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RegistrationTokenCensorTest : BaseTest() {
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

    private val coronaTests: MutableStateFlow<Set<CoronaTest>> = MutableStateFlow(
        setOf(
            mockk<CoronaTest>().apply {
                every { registrationToken } returns testToken
            }
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coronaTestRepository.coronaTests } returns coronaTests
    }

    private fun createInstance() = RegistrationTokenCensor(
        coronaTestRepository = coronaTestRepository
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

        verify { coronaTestRepository.coronaTests }
    }

    @Test
    fun `censoring returns null if there is no token`() = runBlockingTest {
        coronaTests.value = emptySet()

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
