package de.rki.coronawarnapp.nearby.modules.calculationtracker

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.mutate
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
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
            identifier = UUID.randomUUID().toString(),
            state = Calculation.State.CALCULATING,
            startedAt = Instant.EPOCH
        )
        val initialData = mapOf(calcData.identifier to calcData)
        coEvery { storage.load() } returns initialData

        createInstance(scope = this).calculations.first() shouldBe initialData
    }

    @Test
    fun `tracking a new calculation`() = runBlockingTest2(permanentJobs = true) {
        createInstance(scope = this).apply {
            val expectedIdentifier = UUID.randomUUID().toString()
            trackNewCalaculation(expectedIdentifier)

            advanceUntilIdle()

            val calculationData = calculations.first()

            calculationData.entries.single().apply {
                key shouldBe expectedIdentifier
                value shouldBe Calculation(
                    identifier = expectedIdentifier,
                    state = Calculation.State.CALCULATING,
                    startedAt = Instant.EPOCH
                )
            }

            coVerify(ordering = Ordering.ORDERED) {
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
            identifier = UUID.randomUUID().toString(),
            state = Calculation.State.CALCULATING,
            startedAt = Instant.EPOCH
        )
        val initialData = mapOf(calcData.identifier to calcData)
        coEvery { storage.load() } returns initialData

        val expectedData = initialData.mutate {
            this[calcData.identifier] = this[calcData.identifier]!!.copy(
                finishedAt = Instant.EPOCH.plus(1),
                state = Calculation.State.DONE,
                result = Calculation.Result.UPDATED_STATE
            )
        }


        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1)

        createInstance(scope = this).apply {
            finishCalculation(calcData.identifier, Calculation.Result.UPDATED_STATE)

            advanceUntilIdle()

            calculations.first() shouldBe expectedData

            coVerify(ordering = Ordering.ORDERED) {
                storage.load()
                storage.save(any())
                timeStamper.nowUTC
                storage.save(expectedData)
            }
            advanceUntilIdle()
        }
    }

    @Test
    fun `no more than 10 calcluations are tracked`() = runBlockingTest2(permanentJobs = true) {
        val calcData = (1..15L).map {
            val calcData = Calculation(
                identifier = "$it",
                state = Calculation.State.CALCULATING,
                startedAt = Instant.EPOCH.plus(it)
            )
            calcData.identifier to calcData
        }.toMap()

        coEvery { storage.load() } returns calcData

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1)
        createInstance(scope = this).apply {
            finishCalculation("7", Calculation.Result.UPDATED_STATE)

            advanceUntilIdle()

            val data = calculations.first()
            data.size shouldBe 10
            data.values.map { it.identifier }.toList() shouldBe (6..15).map { "$it" }.toList()
        }
    }

    @Test
    fun `60 minute timeout on ongoing calcs`() = runBlockingTest2(permanentJobs = true) {
        every { timeStamper.nowUTC } returns Instant.EPOCH
            .plus(Duration.standardMinutes(60))
            .plus(5)

        // First half will be in the timeout, last half will be ok
        val calcData = (1..10L).map {
            val calcData = Calculation(
                identifier = "$it",
                state = if (it.toInt() % 2 == 0) Calculation.State.CALCULATING else Calculation.State.DONE,
                startedAt = Instant.EPOCH.plus(it)
            )
            calcData.identifier to calcData
        }.toMap()

        coEvery { storage.load() } returns calcData

        createInstance(scope = this).apply {
            advanceUntilIdle()

            calculations.first().apply {
                size shouldBe 8
                values.map { it.identifier } shouldBe listOf(
                    "1",
                    "3",
                    "5",
                    "6",
                    "7",
                    "8",
                    "9",
                    "10"
                )
            }
        }
    }
}
