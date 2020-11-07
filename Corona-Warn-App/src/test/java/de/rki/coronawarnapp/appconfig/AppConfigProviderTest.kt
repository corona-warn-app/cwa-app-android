package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.util.TimeStamper
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
            identifier = "identifier",
            configType = ConfigData.Type.FROM_SERVER
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
        scope = scope
    )

    @Test
    fun `appConfig is observable`() = runBlockingTest2(ignoreActive = true) {
        var counter = 0
        coEvery { source.retrieveConfig() } answers {
            DefaultConfigData(
                serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
                localOffset = Duration.ZERO,
                mappedConfig = configData,
                identifier = "${++counter}",
                configType = ConfigData.Type.FROM_SERVER
            )
        }

        val instance = createInstance(this)

        val testCollector = instance.currentConfig.test(startOnScope = this)

        instance.getAppConfig()
        instance.getAppConfig()
        instance.getAppConfig()

        testCollector.cancel()

        advanceUntilIdle()

        coVerifySequence {
            source.retrieveConfig()
            source.retrieveConfig()
            source.retrieveConfig()
            source.retrieveConfig()
        }
    }

    @Test
    fun `appConfig uses WHILE_SUBSCRIBED mode`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        val testCollector1 = instance.currentConfig.test(startOnScope = this)
        coVerify(exactly = 1) { source.retrieveConfig() }

        // Was still active
        val testCollector2 = instance.currentConfig.test(startOnScope = this)
        advanceUntilIdle()
        testCollector2.cancel()

        // Was still active
        val testCollector3 = instance.currentConfig.test(startOnScope = this)
        advanceUntilIdle()
        testCollector3.cancel()

        coVerify(exactly = 1) { source.retrieveConfig() }
        testCollector1.cancel() // Last subscriber
        advanceUntilIdle()

        // Restarts the HotDataFlow
        val testCollector4 = instance.currentConfig.test(startOnScope = this)
        advanceUntilIdle()
        testCollector4.cancel()

        coVerify(exactly = 2) { source.retrieveConfig() }
    }

    @Test
    fun `clear clears storage and current config`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        instance.clear()

        coVerifySequence {
            source.clear()
        }
    }
}
