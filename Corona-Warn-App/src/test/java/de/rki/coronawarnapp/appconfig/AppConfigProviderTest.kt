package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test
import java.io.File

class AppConfigProviderTest : BaseIOTest() {

    @MockK lateinit var source: AppConfigSource
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private lateinit var testConfigDownload: ConfigData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        testConfigDownload = DefaultConfigData(
            serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
            localOffset = Duration.ZERO,
            mappedConfig = configData,
            isFallback = false
        )
        coEvery { source.clear() } just Runs
        coEvery { source.retrieveConfig() } returns testConfigDownload

        every { timeStamper.nowUTC } returns Instant.parse("2020-11-03T05:35:16.000Z")
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance(scope: CoroutineScope) = AppConfigProvider(
        source = source,
        dispatcherProvider = TestDispatcherProvider,
        scope = scope,
        timeStamper = timeStamper
    )

    @Test
    fun `force update clears caches`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        val testCollector = instance.getConfig().test(startOnScope = this)

        instance.forceUpdate()

        advanceUntilIdle()

        coVerifyOrder {
            source.clear()
        }

        testCollector.cancel()
    }

    @Test
    fun `appConfig is observable`() = runBlockingTest2(ignoreActive = true) {
        // If all observers unsubscribe, the next subscriber triggers an update
        var counter = 0
        val configDatas = mutableListOf<ConfigData>()
        coEvery { source.retrieveConfig() } answers {
            DefaultConfigData(
                serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
                localOffset = Duration.millis((++counter).toLong()),
                mappedConfig = configData,
                isFallback = false
            ).also { configDatas.add(it) }
        }

        val instance = createInstance(this)

        val testCollector = instance.getConfig().test(startOnScope = this)

        advanceUntilIdle()

        instance.forceUpdate()

        testCollector.latestValues.size shouldBe 2
        testCollector.latestValues[0] shouldNotBe testCollector.latestValues[1]
        testCollector.latestValues shouldBe configDatas

        coVerifySequence {
            source.retrieveConfig()

            source.clear()

            source.retrieveConfig()
        }
    }

    @Test
    fun `getConfig tryUpdate overrides timeout check`() = runBlockingTest2(ignoreActive = true) {
        coVerify(exactly = 0) { source.retrieveConfig() }
        val instance = createInstance(this)

        instance.getConfig(tryUpdate = true).test(startOnScope = this).cancel()
        instance.getConfig(tryUpdate = true).test(startOnScope = this).cancel()
        instance.getConfig(tryUpdate = true).test(startOnScope = this).cancel()
        instance.getConfig(tryUpdate = true).test(startOnScope = this).cancel()

        coVerify(exactly = 5) { source.retrieveConfig() }
    }

    @Test
    fun `getConfig uses a 3min timeout check before forcing an update`() =
        runBlockingTest2(ignoreActive = true) {
            var startTime = Instant.parse("2020-11-03T05:35:16.000Z")
            every { timeStamper.nowUTC } returns startTime

            coEvery { source.retrieveConfig() } answers {
                DefaultConfigData(
                    serverTime = startTime,
                    localOffset = Duration.ZERO,
                    mappedConfig = configData,
                    isFallback = false
                )
            }

            val instance = createInstance(this)

            instance.getConfig().test(startOnScope = this).cancel()
            instance.getConfig().test(startOnScope = this).cancel()
            instance.getConfig().test(startOnScope = this).cancel()

            val overTimeout = Duration.standardMinutes(3).plus(1)

            startTime = startTime.plus(overTimeout)
            every { timeStamper.nowUTC } returns startTime

            instance.getConfig().test(startOnScope = this).cancel()
            instance.getConfig().test(startOnScope = this).cancel()

            startTime = startTime.plus(overTimeout)
            every { timeStamper.nowUTC } returns startTime

            instance.getConfig().test(startOnScope = this).cancel()
            instance.getConfig().test(startOnScope = this).cancel()

            coVerify(exactly = 3) { source.retrieveConfig() }
        }
}
