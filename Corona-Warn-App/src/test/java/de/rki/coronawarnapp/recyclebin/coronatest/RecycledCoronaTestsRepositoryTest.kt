package de.rki.coronawarnapp.recyclebin.coronatest

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsRepository
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2

class RecycledCoronaTestsRepositoryTest : BaseTest() {

    @RelaxedMockK private lateinit var coronaTestsRepository: CoronaTestRepository
    @RelaxedMockK private lateinit var recycledCoronaTestsStorage: RecycledCoronaTestsStorage
    @MockK private lateinit var timeStamper: TimeStamper

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    private val recycledPCR = mockk<RecycledCoronaTest> {
        every { recycledAt } returns now
        every { coronaTest } returns mockk<PCRCoronaTest> {
            every { identifier } returns "PCR"
            every { qrCodeHash } returns "PCR_qrCodeHash"
        }
    }

    private val recycledRAT = mockk<RecycledCoronaTest> {
        every { recycledAt } returns now
        every { coronaTest } returns mockk<RACoronaTest> {
            every { identifier } returns "RAT"
            every { qrCodeHash } returns "RAT_qrCodeHash"
        }
    }

    private val testSet = setOf(recycledPCR, recycledRAT)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns now
        coEvery { recycledCoronaTestsStorage.load() } returns testSet
    }

    private fun createInstance(scope: CoroutineScope) = RecycledCoronaTestsRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        coronaTestRepository = coronaTestsRepository,
        recycledCoronaTestsStorage = recycledCoronaTestsStorage,
        timeStamper = timeStamper
    )

    @Test
    fun `restores data from storage storage and does not saved restored data`() =
        runBlockingTest2(ignoreActive = true) {
            coEvery { recycledCoronaTestsStorage.load() } returns emptySet()

            createInstance(this).tests.first() shouldBe emptySet()
            coVerify(exactly = 1) {
                recycledCoronaTestsStorage.load()
            }


            coEvery { recycledCoronaTestsStorage.load() } returns testSet

            createInstance(this).tests.first() shouldBe testSet
            coVerify(exactly = 2) {
                recycledCoronaTestsStorage.load()
            }

            coVerify(exactly = 0) {
                recycledCoronaTestsStorage.save(any())
            }
        }

    @Test
    fun `clear data`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            tests.first() shouldBe testSet

            clear()
            tests.first() shouldBe emptySet()
        }

        coVerify {
            recycledCoronaTestsStorage.save(emptySet())
        }
    }

    @Test
    fun `remove recycled test`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            tests.first() shouldBe testSet

            deleteCoronaTest(recycledPCR)
            tests.first() shouldBe setOf(recycledRAT)

            deleteCoronaTest(recycledRAT)
            tests.first() shouldBe emptySet()

            deleteCoronaTest(recycledRAT)
            tests.first() shouldBe emptySet()
        }

        coVerifyOrder {
            recycledCoronaTestsStorage.save(setOf(recycledRAT))
            recycledCoronaTestsStorage.save(emptySet())
        }
    }

    @Test
    fun `remove all recycled tests`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            tests.first() shouldBe testSet

            deleteAllCoronaTest(testSet)
            tests.first() shouldBe emptySet()
        }

        coVerify {
            recycledCoronaTestsStorage.save(emptySet())
        }
    }

    @Test
    fun `find test for qrCodeHash`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            findCoronaTest(recycledPCR.coronaTest.qrCodeHash) shouldBe recycledPCR
            findCoronaTest(recycledRAT.coronaTest.qrCodeHash) shouldBe recycledRAT
            findCoronaTest("Please return null") shouldBe null
        }
    }
}
