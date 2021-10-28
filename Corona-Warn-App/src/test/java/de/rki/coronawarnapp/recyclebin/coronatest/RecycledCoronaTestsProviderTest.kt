package de.rki.coronawarnapp.recyclebin.coronatest

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class RecycledCoronaTestsProviderTest : BaseTest() {

    @RelaxedMockK private lateinit var coronaTestsRepository: CoronaTestRepository
    @RelaxedMockK private lateinit var recycledCoronaTestsStorage: CoronaTestStorage
    @MockK private lateinit var timeStamper: TimeStamper

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    private val pcrTest = PCRCoronaTest(
        identifier = "PCR",
        registeredAt = now,
        registrationToken = "PCR_registrationToken",
        testResult = CoronaTestResult.PCR_INVALID,
        lastUpdatedAt = now,
        qrCodeHash = "PCR_qrCodeHash",
        recycledAt = null
    )

    private val ratTest = RACoronaTest(
        identifier = "RAT",
        registeredAt = now,
        registrationToken = "RAT_registrationToken",
        testResult = CoronaTestResult.RAT_INVALID,
        testedAt = now,
        lastUpdatedAt = now,
        qrCodeHash = "RAT_qrCodeHash",
        recycledAt = null
    )

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

    private val testSet = setOf(recycledPcrTest, recycledRatTest, pcrTest, ratTest)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns now
        coEvery { recycledCoronaTestsStorage.coronaTests } returns testSet
    }

    private fun createInstance() = RecycledCoronaTestsProvider(
        coronaTestRepository = coronaTestsRepository,
    )

    @Test
    fun `restores data from storage storage and does not saved restored data`() =
        runBlockingTest2(ignoreActive = true) {
            coEvery { recycledCoronaTestsStorage.coronaTests } returns emptySet()

            createInstance().tests.first() shouldBe emptySet()
            coVerify(exactly = 1) {
                recycledCoronaTestsStorage.coronaTests
            }

            coEvery { recycledCoronaTestsStorage.coronaTests } returns testSet

            createInstance().tests.first() shouldBe testSet
            coVerify(exactly = 2) {
                recycledCoronaTestsStorage.coronaTests
            }

            coVerify(exactly = 0) {
                recycledCoronaTestsStorage.coronaTests = any()
            }
        }

    @Test
    fun `clear data`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe testSet
            tests.first() shouldBe emptySet()
        }

        coVerify {
            recycledCoronaTestsStorage.coronaTests = emptySet()
        }
    }

    @Test
    fun `remove recycled test`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe testSet

            deleteCoronaTest(recycledPcrTest.identifier)
            tests.first() shouldBe setOf(recycledRatTest)

            deleteCoronaTest(recycledRatTest.identifier)
            tests.first() shouldBe emptySet()

            deleteCoronaTest(recycledRatTest.identifier)
            tests.first() shouldBe emptySet()
        }

        coVerifyOrder {
            recycledCoronaTestsStorage.coronaTests = setOf(recycledRatTest)
            recycledCoronaTestsStorage.coronaTests = emptySet()
        }
    }

    @Test
    fun `remove all recycled tests`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe testSet

            deleteAllCoronaTest(testSet.map { it.identifier })
            tests.first() shouldBe emptySet()
        }

        coVerify {
            recycledCoronaTestsStorage.coronaTests = emptySet()
        }
    }

    @Test
    fun `find test for qrCodeHash`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            findCoronaTest(recycledPcrTest.qrCodeHash) shouldBe recycledPcrTest
            findCoronaTest(recycledRatTest.qrCodeHash) shouldBe recycledRatTest
            findCoronaTest("Please return null") shouldBe null
            findCoronaTest(null) shouldBe null
        }
    }

    @Test
    fun `restore corona test - throws`() = runBlockingTest2(ignoreActive = true) {
        coEvery { coronaTestsRepository.restoreTest(any()) } throws Exception("Test error")

        createInstance().run {
            assertThrows<Exception> {
                restoreCoronaTest(recycledPcrTest.identifier)
            }

            tests.first() shouldBe testSet
        }

        coVerify {
            coronaTestsRepository.restoreTest(recycledRatTest.identifier)
        }
    }

    @Test
    fun `restore corona test - happy path`() = runBlockingTest2(ignoreActive = true) {
        createInstance().run {
            tests.first() shouldBe testSet

            restoreCoronaTest(recycledRatTest.identifier)
            tests.first() shouldBe setOf(recycledPcrTest)
        }

        coVerify {
            coronaTestsRepository.restoreTest(recycledRatTest.identifier)
        }
    }
}
