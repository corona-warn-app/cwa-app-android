package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
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
            every { clear() } just Runs
        }

        every { parser.parse(testData) } returns statisticsData
        every { foregroundState.isInForeground } returns testForegroundState

        localCache.apply {
            var testLocalCache: ByteArray? = null
            every { load() } answers { testLocalCache }
            every { save(any()) } answers { testLocalCache = arg(0) }
        }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    fun createInstance(scope: CoroutineScope) = StatisticsProvider(
        server = server,
        scope = scope,
        localCache = localCache,
        parser = parser,
        foregroundState = foregroundState,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun `creation is sideeffect free`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this)
        verify(exactly = 0) { localCache.load() }
    }

    @Test
    fun `initial subscription tries cache, then server`() = runBlockingTest2(ignoreActive = true) {
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
    fun `update foreground state change`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        val testCollector = instance.current.test(startOnScope = this)

        testCollector.latestValue shouldBe statisticsData

        val newRawStatisticsData = "Bernd".encodeToByteArray()
        coEvery { server.getRawStatistics() } returns newRawStatisticsData
        val newStatisticsData = mockk<StatisticsData>()
        coEvery { parser.parse(any()) } returns newStatisticsData

        testForegroundState.value = false
        testForegroundState.value = true

        testCollector.latestValues shouldBe listOf(statisticsData, newStatisticsData)
        verify { localCache.save(newRawStatisticsData) }
    }

    @Test
    fun `failed update does not destroy cache`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        val testCollector = instance.current.test(startOnScope = this)

        testCollector.latestValue shouldBe statisticsData

        coEvery { server.getRawStatistics() } throws IOException()

        instance.triggerUpdate()

        testCollector.latestValues shouldBe listOf(statisticsData)
        verify(exactly = 0) { localCache.save(null) }
    }

    @Test
    fun `clear deletes cache`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        val testCollector = instance.current.test(startOnScope = this)
        testCollector.latestValue shouldBe statisticsData

        instance.clear()

        coVerify {
            server.clear()
            localCache.save(null)
        }
    }
}
