package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
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
    private val contactDiaryTestId = "123456-7890-1234-5678-17c2bb69876"

    private val coronaTests = MutableStateFlow(
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

    private val contactDiaryTestResults = MutableStateFlow(
        listOf(
            mockk<ContactDiaryCoronaTestEntity>().apply {
                every { id } returns contactDiaryTestId
            }
        )
    )

    private val contactDiaryTestResults2 = MutableStateFlow(
        listOf(
            mockk<ContactDiaryCoronaTestEntity>().apply {
                every { id } returns contactDiaryTestId
            },
            mockk<ContactDiaryCoronaTestEntity>().apply {
                every { id } returns testToken
            }
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coronaTestRepository.allCoronaTests } returns coronaTests
        every { contactDiaryRepository.testResults } returns contactDiaryTestResults
    }

    private fun createInstance() = CoronaTestCensor(
        debugScope = TestScope(),
        coronaTestRepository = coronaTestRepository,
        contactDiaryRepository = contactDiaryRepository
    )

    @Test
    fun `censoring replaces the logline message even when tests are deleted`() = runTest {
        every { contactDiaryRepository.testResults } returns contactDiaryTestResults2

        val instance = createInstance()
        val filterMe =
            "I'm a shy registration token: $testToken and $contactDiaryTestId"
        instance.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and ########-####-####-####-########9876"

        verify { coronaTestRepository.allCoronaTests }
    }

    @Test
    fun `censoring replaces the logline message`() = runTest {
        val instance = createInstance()
        val filterMe =
            "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier and $contactDiaryTestId"
        instance.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier and ########-####-####-####-########9876"

        verify { coronaTestRepository.allCoronaTests }
    }

    @Test
    fun `censoring returns null if there is no corona test stored`() = runTest {
        coronaTests.value = emptySet()

        val instance = createInstance()
        val filterMeNot =
            "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring returns null if there is no match`() = runTest {
        val instance = createInstance()
        val filterMeNot = "I'm not a registration token ;)"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring still works after test was deleted`() = runTest {

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
