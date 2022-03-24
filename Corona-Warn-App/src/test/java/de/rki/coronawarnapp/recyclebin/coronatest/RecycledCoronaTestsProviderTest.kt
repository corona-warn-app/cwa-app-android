package de.rki.coronawarnapp.recyclebin.coronatest

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class RecycledCoronaTestsProviderTest : BaseTest() {

    @RelaxedMockK private lateinit var coronaTestsRepository: CoronaTestRepository
    @RelaxedMockK private lateinit var familyTestRepository: FamilyTestRepository
    @RelaxedMockK private lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @RelaxedMockK private lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector
    @MockK private lateinit var timeStamper: TimeStamper

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    private val recycledPcrTest = PCRCoronaTest(
        identifier = "PCR-1",
        registeredAt = now,
        registrationToken = "PCR_registrationToken-1",
        testResult = CoronaTestResult.PCR_INVALID,
        lastUpdatedAt = now,
        qrCodeHash = "PCR_qrCodeHash-1",
        recycledAt = now
    )

    private val recycledRatTest = RACoronaTest(
        identifier = "RAT-1",
        registeredAt = now,
        registrationToken = "RAT_registrationToken-1",
        testResult = CoronaTestResult.RAT_INVALID,
        testedAt = now,
        lastUpdatedAt = now,
        qrCodeHash = "RAT_qrCodeHash-1",
        recycledAt = now
    )

    private val recycledTests = setOf(recycledPcrTest, recycledRatTest)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns now
        coEvery { coronaTestsRepository.recycledCoronaTests } returns flowOf(recycledTests)
        coEvery { familyTestRepository.recycledFamilyTests } returns flowOf(setOf())
        coEvery { coronaTestsRepository.removeTest(any()) } returns mockk()
        every { analyticsKeySubmissionCollector.reset(any()) } just Runs
        every { analyticsTestResultCollector.clear(any()) } just Runs
    }

    private fun createInstance() = RecycledCoronaTestsProvider(
        coronaTestRepository = coronaTestsRepository,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        analyticsTestResultCollector = analyticsTestResultCollector,
        familyTestRepository = familyTestRepository
    )

    @Test
    fun `Recycled Tests are retrieved`() =
        runBlockingTest2(ignoreActive = true) {
            createInstance().tests.first() shouldBe recycledTests

            coEvery { coronaTestsRepository.recycledCoronaTests } returns flowOf(emptySet())
            createInstance().tests.first() shouldBe emptySet()

            coVerify(exactly = 2) {
                coronaTestsRepository.recycledCoronaTests
            }
        }

    @Test
    fun `Delete recycled tests one by one`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe recycledTests
            deleteCoronaTest(recycledPcrTest.identifier)
            deleteCoronaTest(recycledRatTest.identifier)
        }

        coVerifyOrder {
            coronaTestsRepository.removeTest(recycledPcrTest.identifier)
            coronaTestsRepository.removeTest(recycledRatTest.identifier)
        }
    }

    @Test
    fun `Delete all recycled tests at once`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe recycledTests
            deleteAllCoronaTest(recycledTests.map { it.identifier })
        }

        coVerify(exactly = 1) {
            coronaTestsRepository.removeTest(recycledPcrTest.identifier)
            coronaTestsRepository.removeTest(recycledRatTest.identifier)
        }
    }

    @Test
    fun `Delete recycled test does not throw if test not found`() = runBlockingTest2(ignoreActive = true) {
        coEvery { coronaTestsRepository.removeTest(any()) } throws CoronaTestNotFoundException("Test error")

        shouldNotThrowAnyUnit {
            createInstance().deleteCoronaTest("I do not exist")
        }

        coVerify {
            coronaTestsRepository.removeTest(any())
        }
    }

    @Test
    fun `Find corona test by qrCodeHash`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            findCoronaTest(recycledPcrTest.qrCodeHash) shouldBe recycledPcrTest
            findCoronaTest(recycledRatTest.qrCodeHash) shouldBe recycledRatTest
            findCoronaTest("Please return null") shouldBe null
            findCoronaTest(null) shouldBe null
        }
    }

    @Test
    fun `Restore RAT corona test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            restoreCoronaTest(recycledRatTest.identifier)
        }

        coVerify {
            coronaTestsRepository.restoreTest(recycledRatTest.identifier)
            analyticsKeySubmissionCollector.reset(any())
            analyticsTestResultCollector.clear(any())
        }
    }

    @Test
    fun `Restore PCR corona test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            restoreCoronaTest(recycledPcrTest.identifier)
        }

        coVerify {
            coronaTestsRepository.restoreTest(recycledPcrTest.identifier)
            analyticsKeySubmissionCollector.reset(any())
        }
    }
}
