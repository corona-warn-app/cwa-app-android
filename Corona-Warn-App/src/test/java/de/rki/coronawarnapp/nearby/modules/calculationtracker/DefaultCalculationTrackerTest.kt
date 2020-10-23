package de.rki.coronawarnapp.nearby.modules.calculationtracker

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.mutate
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import java.util.UUID

class DefaultCalculationTrackerTest : BaseTest() {

    @MockK lateinit var storage: CalculationTrackerStorage
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.EPOCH
        coEvery { storage.load() } returns emptyMap()
        coEvery { storage.save(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope) = DefaultCalculationTracker(
        scope = scope,
        dispatcherProvider = TestDispatcherProvider,
        storage = storage,
        timeStamper = timeStamper
    )

    @Test
    fun `side effect free init`() = runBlockingTest {
        createInstance(scope = this)
        verify { storage wasNot Called }
        verify { timeStamper wasNot Called }
    }

    @Test
    fun `data is restored from storage`() = runBlockingTest2(permanentJobs = true) {
        val calcData = Calculation(
            token = UUID.randomUUID().toString(),
            state = Calculation.State.CALCULATING,
            startedAt = Instant.EPOCH
        )
        val initialData = mapOf(calcData.token to calcData)
        coEvery { storage.load() } returns initialData

        createInstance(scope = this).calculations.first() shouldBe initialData
    }

    @Test
    fun `tracking a new calculation`() = runBlockingTest2(permanentJobs = true) {
        createInstance(scope = this).apply {
            val expectedToken = UUID.randomUUID().toString()
            trackNewCalaculation(expectedToken)

            advanceUntilIdle()

            val calculationData = calculations.first()

            calculationData.entries.single().apply {
                key shouldBe expectedToken
                value shouldBe Calculation(
                    token = expectedToken,
                    state = Calculation.State.CALCULATING,
                    startedAt = Instant.EPOCH
                )
            }

            coVerifySequence {
                storage.load()
                storage.save(emptyMap())
                timeStamper.nowUTC
                storage.save(calculationData)
            }
            advanceUntilIdle()
        }
    }

    @Test
    fun `finish an existing calcluation`() = runBlockingTest2(permanentJobs = true) {
        val calcData = Calculation(
            token = UUID.randomUUID().toString(),
            state = Calculation.State.CALCULATING,
            startedAt = Instant.EPOCH
        )
        val initialData = mapOf(calcData.token to calcData)
        coEvery { storage.load() } returns initialData

        val expectedData = initialData.mutate {
            this[calcData.token] = this[calcData.token]!!.copy(
                finishedAt = Instant.EPOCH.plus(1),
                state = Calculation.State.DONE,
                result = Calculation.Result.UPDATED_STATE
            )
        }


        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1)

        createInstance(scope = this).apply {
            finishCalculation(calcData.token, Calculation.Result.UPDATED_STATE)

            advanceUntilIdle()

            calculations.first() shouldBe expectedData

            coVerifySequence {
                storage.load()
                storage.save(any())
                timeStamper.nowUTC
                storage.save(expectedData)
            }
            advanceUntilIdle()
        }
    }
}
