package de.rki.coronawarnapp.coronatest.type.pcr.execution

import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
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
        appScope = TestScope(),
        coronaTestRepository = coronaTestRepository,
        workManager = workManager
    )

    @Test
    fun `final worker doesn't need to be scheduled`() {
        every { coronaTestRepository.coronaTests } returns flowOf(
            setOf(
                mockk<PCRCoronaTest>().apply {
                    every { isRedeemed } returns true
                    every { type } returns BaseCoronaTest.Type.PCR
                }
            )
        )

        runTest {
            createInstance().shouldBePolling.first() shouldBe false
        }
    }

    @Test
    fun `not final worker needs to be scheduled`() {
        every { coronaTestRepository.coronaTests } returns flowOf(
            setOf(
                mockk<PCRCoronaTest>().apply {
                    every { isRedeemed } returns false
                    every { type } returns BaseCoronaTest.Type.PCR
                }
            )
        )

        runTest {
            createInstance().shouldBePolling.first() shouldBe true
        }
    }

    @Test
    fun `no worker needed without test`() {
        every { coronaTestRepository.coronaTests } returns flowOf(emptySet())

        runTest {
            createInstance().shouldBePolling.first() shouldBe false
        }
    }
}
