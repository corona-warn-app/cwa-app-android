package de.rki.coronawarnapp.nearby.modules.detectiontracker

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
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
import kotlinx.coroutines.flow.flowOf
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

class DefaultExposureDetectionTrackerTest : BaseTest() {

    @MockK lateinit var storage: ExposureDetectionTrackerStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var configProvider: AppConfigProvider
    @MockK lateinit var appConfigData: ConfigData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.EPOCH
        coEvery { storage.load() } returns emptyMap()
        coEvery { storage.save(any()) } just Runs

        coEvery { configProvider.currentConfig } returns flowOf(appConfigData)
        every { appConfigData.overallDetectionTimeout } returns Duration.standardMinutes(15)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope) = DefaultExposureDetectionTracker(
        scope = scope,
        dispatcherProvider = TestDispatcherProvider,
        storage = storage,
        timeStamper = timeStamper,
        appConfigProvider = configProvider
    )

    @Test
    fun `side effect free init`() = runBlockingTest {
        createInstance(scope = this)
        verify { storage wasNot Called }
        verify { timeStamper wasNot Called }
    }

    @Test
    fun `data is restored from storage`() = runBlockingTest2(ignoreActive = true) {
        val calcData = TrackedExposureDetection(
            identifier = UUID.randomUUID().toString(),
            startedAt = Instant.EPOCH
        )
        val initialData = mapOf(calcData.identifier to calcData)
        coEvery { storage.load() } returns initialData

        createInstance(scope = this).calculations.first() shouldBe initialData
    }

    @Test
    fun `tracking a new calculation`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).apply {
            val expectedIdentifier = UUID.randomUUID().toString()
            trackNewExposureDetection(expectedIdentifier)

            advanceUntilIdle()

            val calculationData = calculations.first()

            calculationData.entries.single().apply {
                key shouldBe expectedIdentifier
                value shouldBe TrackedExposureDetection(
                    identifier = expectedIdentifier,
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

        coVerify { configProvider.currentConfig }
    }

    @Test
    fun `finish an existing calcluation`() = runBlockingTest2(ignoreActive = true) {
        val calcData = TrackedExposureDetection(
            identifier = UUID.randomUUID().toString(),
            startedAt = Instant.EPOCH
        )
        val initialData = mapOf(calcData.identifier to calcData)
        coEvery { storage.load() } returns initialData

        val expectedData = initialData.mutate {
            this[calcData.identifier] = this[calcData.identifier]!!.copy(
                finishedAt = Instant.EPOCH.plus(1),
                result = TrackedExposureDetection.Result.UPDATED_STATE
            )
        }

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1)

        createInstance(scope = this).apply {
            finishExposureDetection(calcData.identifier, TrackedExposureDetection.Result.UPDATED_STATE)

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
    fun `a late calculation overwrites timeout state`() = runBlockingTest2(ignoreActive = true) {
        val calcData = TrackedExposureDetection(
            identifier = UUID.randomUUID().toString(),
            startedAt = Instant.EPOCH,
            finishedAt = Instant.EPOCH.plus(1),
            result = TrackedExposureDetection.Result.TIMEOUT
        )
        val initialData = mapOf(calcData.identifier to calcData)
        coEvery { storage.load() } returns initialData

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(2)

        val expectedData = initialData.mutate {
            this[calcData.identifier] = this[calcData.identifier]!!.copy(
                finishedAt = Instant.EPOCH.plus(2),
                result = TrackedExposureDetection.Result.UPDATED_STATE
            )
        }

        createInstance(scope = this).apply {
            finishExposureDetection(calcData.identifier, TrackedExposureDetection.Result.UPDATED_STATE)

            advanceUntilIdle()

            calculations.first() shouldBe expectedData
        }
    }

    @Test
    fun `no more than 10 calcluations are tracked`() = runBlockingTest2(ignoreActive = true) {
        val calcData = (1..15L).map {
            val calcData = TrackedExposureDetection(
                identifier = "$it",
                startedAt = Instant.EPOCH.plus(it)
            )
            calcData.identifier to calcData
        }.toMap()

        coEvery { storage.load() } returns calcData

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1)
        createInstance(scope = this).apply {
            finishExposureDetection("7", TrackedExposureDetection.Result.UPDATED_STATE)

            advanceUntilIdle()

            val data = calculations.first()
            data.size shouldBe 5
            data.values.map { it.identifier }.toList() shouldBe (11..15).map { "$it" }.toList()
        }
    }

    @Test
    fun `15 minute timeout on ongoing calcs`() = runBlockingTest2(ignoreActive = true) {
        every { timeStamper.nowUTC } returns Instant.EPOCH
            .plus(Duration.standardMinutes(15))
            .plus(2)

        // First half will be in the timeout, last half will be ok
        val timeoutOnRunningCalc = TrackedExposureDetection(
            identifier = "0",
            startedAt = Instant.EPOCH
        )
        val timeoutonRunningCalc2 = TrackedExposureDetection(
            identifier = "1",
            startedAt = Instant.EPOCH.plus(1)
        )
        // We shouldn't care for timeouts on finished calculations
        val timeoutIgnoresFinishedCalcs = TrackedExposureDetection(
            identifier = "2",
            startedAt = Instant.EPOCH.plus(1),
            finishedAt = Instant.EPOCH.plus(15)
        )

        // This one is right on the edge, testing <= behavior
        val timeoutRunningOnEdge = TrackedExposureDetection(
            identifier = "3",
            startedAt = Instant.EPOCH.plus(2)
        )

        val noTimeoutCalcRunning = TrackedExposureDetection(
            identifier = "4",
            startedAt = Instant.EPOCH.plus(4)
        )
        val noTimeOutCalcFinished = TrackedExposureDetection(
            identifier = "5",
            startedAt = Instant.EPOCH.plus(5),
            finishedAt = Instant.EPOCH.plus(15)
        )

        val calcData = mapOf(
            timeoutOnRunningCalc.identifier to timeoutOnRunningCalc,
            timeoutonRunningCalc2.identifier to timeoutonRunningCalc2,
            timeoutIgnoresFinishedCalcs.identifier to timeoutIgnoresFinishedCalcs,
            timeoutRunningOnEdge.identifier to timeoutRunningOnEdge,
            noTimeoutCalcRunning.identifier to noTimeoutCalcRunning,
            noTimeOutCalcFinished.identifier to noTimeOutCalcFinished
        )

        coEvery { storage.load() } returns calcData

        createInstance(scope = this).apply {
            advanceUntilIdle()

            calculations.first().apply {
                size shouldBe 6

                this["0"] shouldBe timeoutOnRunningCalc.copy(
                    finishedAt = timeStamper.nowUTC,
                    result = TrackedExposureDetection.Result.TIMEOUT
                )
                this["1"] shouldBe timeoutonRunningCalc2.copy(
                    finishedAt = timeStamper.nowUTC,
                    result = TrackedExposureDetection.Result.TIMEOUT
                )
                this["2"] shouldBe timeoutIgnoresFinishedCalcs

                this["3"] shouldBe timeoutRunningOnEdge

                this["4"] shouldBe noTimeoutCalcRunning
                this["5"] shouldBe noTimeOutCalcFinished
            }
        }
    }
}
