package de.rki.coronawarnapp.recyclebin.coronatest

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrowAny
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

    @RelaxedMockK private lateinit var coronaTestRepository: CoronaTestRepository
    @RelaxedMockK private lateinit var familyTestRepository: FamilyTestRepository
    @RelaxedMockK private lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @RelaxedMockK private lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector
    @MockK private lateinit var timeStamper: TimeStamper

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    private val recycledPersonalPcrTest = PCRCoronaTest(
        identifier = "PCR-1",
        registeredAt = now,
        registrationToken = "PCR_registrationToken-1",
        testResult = CoronaTestResult.PCR_INVALID,
        lastUpdatedAt = now,
        qrCodeHash = "PCR_qrCodeHash-1",
        recycledAt = now
    )

    private val recycledPersonalRatTest = RACoronaTest(
        identifier = "RAT-1",
        registeredAt = now,
        registrationToken = "RAT_registrationToken-1",
        testResult = CoronaTestResult.RAT_INVALID,
        testedAt = now,
        lastUpdatedAt = now,
        qrCodeHash = "RAT_qrCodeHash-1",
        recycledAt = now
    )

    private val recycledFamilyRatTest = FamilyCoronaTest(
        coronaTest = CoronaTest(
            type = BaseCoronaTest.Type.RAPID_ANTIGEN,
            identifier = "RAT-1f",
            registeredAt = now,
            registrationToken = "RAT_registrationToken-1f",
            testResult = CoronaTestResult.RAT_INVALID,
            qrCodeHash = "RAT_qrCodeHash-1f",
            recycledAt = now,
        ),
        personName = "Happy Person"
    )

    private val recycledFamilyPcrTest = FamilyCoronaTest(
        coronaTest = CoronaTest(
            type = BaseCoronaTest.Type.PCR,
            identifier = "PCR-1f",
            registeredAt = now,
            registrationToken = "PCR_registrationToken-1f",
            testResult = CoronaTestResult.PCR_INVALID,
            qrCodeHash = "PCR_qrCodeHash-1f",
            recycledAt = now,
        ),
        personName = "Traveler has PCR"
    )

    private val recycledPersonalTests = setOf(recycledPersonalPcrTest, recycledPersonalRatTest)
    private val recycledFamilyTests = setOf(recycledFamilyPcrTest, recycledFamilyRatTest)

    private val allTests = recycledPersonalTests + recycledFamilyTests

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns now
        coEvery { coronaTestRepository.recycledTests } returns flowOf(recycledPersonalTests)
        coEvery { coronaTestRepository.removeTest(any()) } returns mockk()

        coEvery { familyTestRepository.familyTestRecycleBin } returns flowOf(recycledFamilyTests)
        coEvery { familyTestRepository.removeTest(any()) } returns mockk()

        every { analyticsKeySubmissionCollector.reset(any()) } just Runs
        every { analyticsTestResultCollector.clear(any()) } just Runs
    }

    private fun createInstance() = RecycledCoronaTestsProvider(
        coronaTestRepository = coronaTestRepository,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        analyticsTestResultCollector = analyticsTestResultCollector,
        familyTestRepository = familyTestRepository
    )

    @Test
    fun `Recycled Tests are retrieved`() =
        runBlockingTest2(ignoreActive = true) {
            createInstance().tests.first() shouldBe allTests

            coVerify {
                coronaTestRepository.recycledTests
                familyTestRepository.familyTestRecycleBin
            }
        }

    @Test
    fun `Delete recycled tests one by one`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe allTests
            deleteCoronaTest(recycledPersonalPcrTest.identifier)
            deleteCoronaTest(recycledPersonalRatTest.identifier)

            deleteCoronaTest(recycledFamilyRatTest.identifier)
            deleteCoronaTest(recycledFamilyPcrTest.identifier)
        }

        coVerifyOrder {
            coronaTestRepository.removeTest(recycledPersonalPcrTest.identifier)
            coronaTestRepository.removeTest(recycledPersonalRatTest.identifier)

            familyTestRepository.removeTest(recycledFamilyRatTest.identifier)
            familyTestRepository.removeTest(recycledFamilyPcrTest.identifier)
        }
    }

    @Test
    fun `Delete all recycled tests at once`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe allTests
            deleteAllCoronaTest(allTests.map { it.identifier })
        }

        coVerify(exactly = 1) {
            coronaTestRepository.removeTest(recycledPersonalPcrTest.identifier)
            coronaTestRepository.removeTest(recycledPersonalRatTest.identifier)

            familyTestRepository.removeTest(recycledFamilyPcrTest.identifier)
            familyTestRepository.removeTest(recycledFamilyRatTest.identifier)
        }
    }

    @Test
    fun `Delete all recycled personal tests at once`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe allTests
            deleteAllCoronaTest(recycledPersonalTests.map { it.identifier })
        }

        coVerify(exactly = 1) {
            coronaTestRepository.removeTest(recycledPersonalPcrTest.identifier)
            coronaTestRepository.removeTest(recycledPersonalRatTest.identifier)
        }

        coVerify(exactly = 0) {
            familyTestRepository.removeTest(recycledFamilyPcrTest.identifier)
            familyTestRepository.removeTest(recycledFamilyRatTest.identifier)
        }
    }

    @Test
    fun `Delete all recycled family tests at once`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe allTests
            deleteAllCoronaTest(recycledFamilyTests.map { it.identifier })
        }

        coVerify(exactly = 0) {
            coronaTestRepository.removeTest(recycledPersonalPcrTest.identifier)
            coronaTestRepository.removeTest(recycledPersonalRatTest.identifier)
        }

        coVerify(exactly = 1) {
            familyTestRepository.removeTest(recycledFamilyPcrTest.identifier)
            familyTestRepository.removeTest(recycledFamilyRatTest.identifier)
        }
    }

    @Test
    fun `Delete recycled test does not throw if test not found`() = runBlockingTest2(ignoreActive = true) {
        coEvery { coronaTestRepository.removeTest(any()) } throws CoronaTestNotFoundException("Test error")

        shouldNotThrowAny {
            createInstance().deleteCoronaTest("I do not exist")
        }

        coVerify(exactly = 0) {
            coronaTestRepository.removeTest(any())
            familyTestRepository.removeTest(any())
        }
    }

    @Test
    fun `Find corona test by qrCodeHash`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            findCoronaTest(recycledPersonalPcrTest.qrCodeHash) shouldBe recycledPersonalPcrTest
            findCoronaTest(recycledPersonalRatTest.qrCodeHash) shouldBe recycledPersonalRatTest

            findCoronaTest(recycledFamilyPcrTest.qrCodeHash) shouldBe recycledFamilyPcrTest
            findCoronaTest(recycledFamilyRatTest.qrCodeHash) shouldBe recycledFamilyRatTest

            findCoronaTest("Please return null") shouldBe null
            findCoronaTest(null) shouldBe null
        }
    }

    @Test
    fun `Restore Personal RAT corona test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            restoreCoronaTest(recycledPersonalRatTest.identifier)
        }

        coVerify {
            coronaTestRepository.restoreTest(recycledPersonalRatTest.identifier)
            analyticsKeySubmissionCollector.reset(any())
            analyticsTestResultCollector.clear(any())
        }
    }

    @Test
    fun `Restore Personal PCR corona test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            restoreCoronaTest(recycledPersonalPcrTest.identifier)
        }

        coVerify {
            coronaTestRepository.restoreTest(recycledPersonalPcrTest.identifier)
            analyticsKeySubmissionCollector.reset(any())
        }
    }

    @Test
    fun `Restore Family RAT corona test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            restoreCoronaTest(recycledFamilyRatTest.identifier)
        }

        coVerify {
            familyTestRepository.restoreTest(recycledFamilyRatTest.identifier)
        }

        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reset(any())
            analyticsTestResultCollector.clear(any())
        }
    }

    @Test
    fun `Restore Family PCR corona test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            restoreCoronaTest(recycledFamilyPcrTest.identifier)
        }

        coVerify {
            familyTestRepository.restoreTest(recycledFamilyPcrTest.identifier)
        }

        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reset(any())
        }
    }
}
