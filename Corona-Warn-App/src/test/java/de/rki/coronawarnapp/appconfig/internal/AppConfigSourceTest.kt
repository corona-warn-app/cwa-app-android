package de.rki.coronawarnapp.appconfig.internal

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.sources.fallback.DefaultAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.local.LocalAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.remote.RemoteAppConfigSource
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AppConfigSourceTest : BaseTest() {

    @MockK lateinit var remoteSource: RemoteAppConfigSource
    @MockK lateinit var localSource: LocalAppConfigSource
    @MockK lateinit var defaultSource: DefaultAppConfigSource
    @MockK lateinit var timeStamper: TimeStamper

    private val remoteConfig = ConfigDataContainer(
        serverTime = Instant.EPOCH,
        localOffset = Duration.standardHours(1),
        mappedConfig = mockk(),
        configType = ConfigData.Type.FROM_SERVER,
        identifier = "remoteetag",
        cacheValidity = Duration.standardSeconds(42)
    )

    private val localConfig = ConfigDataContainer(
        serverTime = Instant.EPOCH,
        localOffset = Duration.standardHours(1),
        mappedConfig = mockk(),
        configType = ConfigData.Type.LAST_RETRIEVED,
        identifier = "localetag",
        cacheValidity = Duration.standardSeconds(300)
    )

    private val defaultConfig = ConfigDataContainer(
        serverTime = Instant.EPOCH,
        localOffset = Duration.standardHours(1),
        mappedConfig = mockk(),
        configType = ConfigData.Type.LOCAL_DEFAULT,
        identifier = "fallback.local",
        cacheValidity = Duration.ZERO
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { remoteSource.getConfigData() } returns remoteConfig
        coEvery { localSource.getConfigData() } returns localConfig
        coEvery { defaultSource.getConfigData() } returns defaultConfig

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(1))
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = AppConfigSource(
        remoteAppConfigSource = remoteSource,
        localAppConfigSource = localSource,
        defaultAppConfigSource = defaultSource,
        timeStamper = timeStamper
    )

    @Test
    fun `local config is used if available and valid`() = runBlockingTest {
        val instance = createInstance()
        instance.getConfigData() shouldBe localConfig

        coVerifySequence {
            localSource.getConfigData()
            timeStamper.nowUTC
        }
    }

    @Test
    fun `remote config is used if local config is not valid`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH
            .plus(Duration.standardHours(1))
            .plus(Duration.standardSeconds(301)) // Local config has 300 seconds validity

        val instance = createInstance()
        instance.getConfigData() shouldBe remoteConfig

        coVerifySequence {
            localSource.getConfigData()
            timeStamper.nowUTC
            remoteSource.getConfigData()
        }
    }

    @Test
    fun `local config is used despite being invalid if remote config is unavailable`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(2))
        coEvery { remoteSource.getConfigData() } returns null

        val instance = createInstance()
        instance.getConfigData() shouldBe localConfig

        coVerifySequence {
            localSource.getConfigData()
            timeStamper.nowUTC
            remoteSource.getConfigData()
        }
    }

    @Test
    fun `default config is used if remote and local are unavailable`() = runBlockingTest {
        coEvery { remoteSource.getConfigData() } returns null
        coEvery { localSource.getConfigData() } returns null

        val instance = createInstance()
        instance.getConfigData() shouldBe defaultConfig

        coVerifySequence {
            localSource.getConfigData()
            remoteSource.getConfigData()
            defaultSource.getConfigData()
        }
    }
}
