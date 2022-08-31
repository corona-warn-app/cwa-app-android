package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class CoronaTestCensorTest : BaseTest() {
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"
    private val pcrIdentifier = "qrcode-pcr-someIdentifier"
    private val ratIdentifier = "qrcode-rat-someIdentifier"
    private val pcrAuthCode = "qrcode-pcr-authcode"
    private val ratAuthCode = "qrcode-rat-authcode"
    private val contactDiaryTestId = "123456-7890-1234-5678-17c2bb69876"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coronaTestRepository.allCoronaTests } returns flowOf(
            setOf(
                mockk<PCRCoronaTest>().apply {
                    every { registrationToken } returns testToken
                    every { identifier } returns pcrIdentifier
                    every { authCode } returns pcrAuthCode
                },
                mockk<RACoronaTest>().apply {
                    every { registrationToken } returns testToken
                    every { identifier } returns ratIdentifier
                    every { authCode } returns ratAuthCode
                }
            )
        )
        every { contactDiaryRepository.testResults } returns flowOf(
            listOf(
                mockk<ContactDiaryCoronaTestEntity>().apply {
                    every { id } returns contactDiaryTestId
                }
            )
        )
    }

    private fun createInstance(scope: CoroutineScope) = CoronaTestCensor(
        debugScope = scope,
        coronaTestRepository = coronaTestRepository,
        contactDiaryRepository = contactDiaryRepository
    )

    @Test
    fun `censoring replaces the log line message even when tests are deleted`() = runTest(UnconfinedTestDispatcher()) {
        every { contactDiaryRepository.testResults } returns flowOf(
            listOf(
                mockk<ContactDiaryCoronaTestEntity>().apply {
                    every { id } returns contactDiaryTestId
                },
                mockk<ContactDiaryCoronaTestEntity>().apply {
                    every { id } returns testToken
                }
            )
        )

        val instance = createInstance(this)
        val filterMe =
            "I'm a shy registration token: $testToken and $contactDiaryTestId"
        instance.checkLog(filterMe)!!
            .compile()!!.censored.checkCensoredLogLine()

        verify { coronaTestRepository.allCoronaTests }
    }

    @Test
    fun `censoring replaces the log line message`() = runTest(UnconfinedTestDispatcher()) {
        val instance = createInstance(this)
        val filterMe =
            "I'm a shy registration token: $testToken and we are extrovert " +
                "$pcrIdentifier and $ratIdentifier and $contactDiaryTestId" +
                " and $pcrAuthCode and $ratAuthCode"

        instance.checkLog(filterMe)!!.compile()!!.censored.checkCensoredLogLine()

        verify { coronaTestRepository.allCoronaTests }
    }

    @Test
    fun `censoring returns null if there is no corona test stored`() = runTest(UnconfinedTestDispatcher()) {
        every { coronaTestRepository.allCoronaTests } returns flowOf(emptySet())

        val instance = createInstance(this)
        val filterMeNot =
            "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring returns null if there is no match`() = runTest(UnconfinedTestDispatcher()) {
        val instance = createInstance(this)
        val filterMeNot = "I'm not a registration token ;)"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring still works after test was deleted`() = runTest(UnconfinedTestDispatcher()) {

        val censor = createInstance(this)

        val filterMe = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"

        censor.checkLog(filterMe)!!
            .compile()!!.censored.checkCensoredLogLine()
        // delete all tests
        every { coronaTestRepository.allCoronaTests } returns flowOf(emptySet())

        censor.checkLog(filterMe)!!
            .compile()!!.censored.checkCensoredLogLine()
    }

    private fun String.checkCensoredLogLine() {
        shouldNotContain(testToken)
        shouldNotContain(pcrIdentifier)
        shouldNotContain(ratIdentifier)
        shouldNotContain(contactDiaryTestId)
        shouldNotContain(pcrAuthCode)
        shouldNotContain(ratAuthCode)
    }
}
