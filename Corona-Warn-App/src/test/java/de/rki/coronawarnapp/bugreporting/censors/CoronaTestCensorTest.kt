package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class CoronaTestCensorTest : BaseTest() {
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"
    private val pcrIdentifier = "qrcode-pcr-someIdentifier"
    private val ratIdentifier = "qrcode-rat-someIdentifier"

    private val coronaTests: MutableStateFlow<Set<CoronaTest>> = MutableStateFlow(
        setOf(
            mockk<PCRCoronaTest>().apply {
                every { registrationToken } returns testToken
                every { identifier } returns pcrIdentifier
            },
            mockk<RACoronaTest>().apply {
                every { registrationToken } returns testToken
                every { identifier } returns ratIdentifier
            }
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coronaTestRepository.coronaTests } returns coronaTests
    }

    private fun createInstance() = CoronaTestCensor(
        debugScope = TestCoroutineScope(),
        coronaTestRepository = coronaTestRepository
    )

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        val instance = createInstance()
        val filterMe = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"
        instance.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"

        verify { coronaTestRepository.coronaTests }
    }

    @Test
    fun `censoring returns null if there is no corona test stored`() = runBlockingTest {
        coronaTests.value = emptySet()

        val instance = createInstance()
        val filterMeNot =
            "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring returns null if there is no match`() = runBlockingTest {
        val instance = createInstance()
        val filterMeNot = "I'm not a registration token ;)"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring still works after test was deleted`() = runBlockingTest {

        val censor = createInstance()

        val filterMe = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"

        censor.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"

        // delete all tests
        coronaTests.value = emptySet()

        censor.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"
    }
}
