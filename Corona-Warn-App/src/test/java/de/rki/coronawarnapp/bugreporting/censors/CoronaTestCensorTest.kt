package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

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

        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false

        every { coronaTestRepository.coronaTests } returns coronaTests
    }

    private fun createInstance() = CoronaTestCensor(
        coronaTestRepository = coronaTestRepository
    )

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        val instance = createInstance()
        val filterMe = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(filterMe) shouldBe filterMe.copy(
            message = "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"
        )

        every { CWADebug.isDeviceForTestersBuild } returns true
        instance.checkLog(filterMe) shouldBe filterMe.copy(
            message = "I'm a shy registration token: ########-e0de-4bd4-90c1-17c2bb683a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"
        )

        verify { coronaTestRepository.coronaTests }
    }

    @Test
    fun `censoring returns null if there is no corona test stored`() = runBlockingTest {
        coronaTests.value = emptySet()

        val instance = createInstance()
        val filterMeNot = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier",
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
