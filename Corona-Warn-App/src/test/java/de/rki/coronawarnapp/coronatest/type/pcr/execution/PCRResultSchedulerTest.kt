package de.rki.coronawarnapp.coronatest.type.pcr.execution

import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.covidcertificate.test.CoronaTestRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PCRResultSchedulerTest : BaseTest() {

    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { workManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
    }

    private fun createInstance() = PCRResultScheduler(
        appScope = TestCoroutineScope(),
        coronaTestRepository = coronaTestRepository,
        workManager = workManager
    )

    @Test
    fun `final worker doesn't need to be scheduled`() {
        every { coronaTestRepository.coronaTests } returns flowOf(
            setOf(
                mockk<PCRCoronaTest>().apply {
                    every { isFinal } returns true
                    every { type } returns CoronaTest.Type.PCR
                }
            )
        )

        runBlockingTest {
            createInstance().shouldBePolling.first() shouldBe false
        }
    }

    @Test
    fun `not final worker needs to be scheduled`() {
        every { coronaTestRepository.coronaTests } returns flowOf(
            setOf(
                mockk<PCRCoronaTest>().apply {
                    every { isFinal } returns false
                    every { type } returns CoronaTest.Type.PCR
                }
            )
        )

        runBlockingTest {
            createInstance().shouldBePolling.first() shouldBe true
        }
    }

    @Test
    fun `no worker needed without test`() {
        every { coronaTestRepository.coronaTests } returns flowOf(emptySet())

        runBlockingTest {
            createInstance().shouldBePolling.first() shouldBe false
        }
    }
}
