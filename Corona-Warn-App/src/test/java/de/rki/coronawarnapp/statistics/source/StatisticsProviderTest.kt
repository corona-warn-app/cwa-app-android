package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider
import testhelpers.coroutines.runTest2
import testhelpers.coroutines.test
import java.io.IOException

class StatisticsProviderTest : BaseTest() {
    @MockK lateinit var server: StatisticsServer
    @MockK lateinit var localCache: StatisticsCache
    @MockK lateinit var parser: StatisticsParser
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var statisticsData: StatisticsData

    private val testData = "ABC".encodeToByteArray()

    private val testForegroundState = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        server.apply {
            coEvery { getRawStatistics() } returns testData
        }

        every { parser.parse(testData) } returns statisticsData
        every { foregroundState.isInForeground } returns testForegroundState
        every { statisticsData.isDataAvailable } returns true

        localCache.apply {
            var testLocalCache: ByteArray? = null
            every { load() } answers { testLocalCache }
            every { save(any()) } answers { testLocalCache = arg(0) }
        }
    }

    fun createInstance(scope: CoroutineScope) = StatisticsProvider(
        server = server,
        scope = scope,
        localCache = localCache,
        parser = parser,
        foregroundState = foregroundState,
        dispatcherProvider = scope.asDispatcherProvider()
    )

    @Test
    fun `creation is side effect free`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            createInstance(this)
            verify(exactly = 0) { localCache.load() }
        }

    @Test
    fun `initial subscription tries cache, then server`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val testCollector = createInstance(this).current.test(startOnScope = this)

            coVerifySequence {
                localCache.load()
                server.getRawStatistics()
                parser.parse(testData)
                localCache.save(testData)
            }

            testCollector.latestValue shouldBe statisticsData
        }

    @Test
    fun `update foreground state change`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)
            val testCollector = instance.current.test(startOnScope = this)

            testCollector.latestValues shouldBe listOf(StatisticsData.DEFAULT, statisticsData)

            val newRawStatisticsData = "Bernd".encodeToByteArray()
            coEvery { server.getRawStatistics() } returns newRawStatisticsData
            val newStatisticsData = mockk<StatisticsData>().apply {
                every { isDataAvailable } returns false
            }
            coEvery { parser.parse(any()) } returns newStatisticsData

            testForegroundState.value = false
            testForegroundState.value = true

            testCollector.latestValues shouldBe listOf(StatisticsData.DEFAULT, statisticsData, newStatisticsData)
            verify { localCache.save(newRawStatisticsData) }
        }

    @Test
    fun `failed update does not destroy cache`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)
            val testCollector = instance.current.test(startOnScope = this)

            testCollector.latestValues shouldBe listOf(StatisticsData.DEFAULT, statisticsData)

            coEvery { server.getRawStatistics() } throws IOException()

            instance.triggerUpdate()

            testCollector.latestValues shouldBe listOf(StatisticsData.DEFAULT, statisticsData)
        }

    @Test
    fun `subscription flow timeout is 5 seconds`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)
            var testCollector1 = instance.current.test(startOnScope = this)
            var testCollector2 = instance.current.test(startOnScope = this)

            advanceUntilIdle()
            coVerify(exactly = 1) { localCache.load() }

            testCollector1.cancel()
            testCollector2.cancel()

            testScheduler.apply { advanceTimeBy(6000); runCurrent() }

            testCollector1 = instance.current.test(startOnScope = this)
            testCollector2 = instance.current.test(startOnScope = this)

            advanceUntilIdle()
            coVerify(exactly = 2) { localCache.load() }

            testCollector1.cancel()
            testCollector2.cancel()
        }
}
