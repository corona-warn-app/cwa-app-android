package de.rki.coronawarnapp.appconfig.internal

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import de.rki.coronawarnapp.appconfig.sources.fallback.DefaultAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.local.LocalAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.remote.RemoteAppConfigSource
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AppConfigSourceTest : BaseTest() {

    @MockK lateinit var remoteSource: RemoteAppConfigSource
    @MockK lateinit var localSource: LocalAppConfigSource
    @MockK lateinit var defaultSource: DefaultAppConfigSource
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var cwaSettings: CWASettings

    private val remoteConfig = ConfigDataContainer(
        serverTime = Instant.EPOCH,
        localOffset = Duration.standardHours(1),
        mappedConfig = mockk<ConfigMapping>().apply {
            every { isDeviceTimeCheckEnabled } returns true
        },
        configType = ConfigData.Type.FROM_SERVER,
        identifier = "remoteetag",
        cacheValidity = Duration.standardSeconds(42)
    )

    private val localConfig = ConfigDataContainer(
        serverTime = Instant.EPOCH,
        localOffset = Duration.standardHours(1),
        mappedConfig = mockk<ConfigMapping>().apply {
            every { isDeviceTimeCheckEnabled } returns true
        },
        configType = ConfigData.Type.LAST_RETRIEVED,
        identifier = "localetag",
        cacheValidity = Duration.standardSeconds(300)
    )

    private val defaultConfig = ConfigDataContainer(
        serverTime = Instant.EPOCH,
        localOffset = Duration.standardHours(1),
        mappedConfig = mockk<ConfigMapping>().apply {
            every { isDeviceTimeCheckEnabled } returns true
        },
        configType = ConfigData.Type.LOCAL_DEFAULT,
        identifier = "fallback.local",
        cacheValidity = Duration.ZERO
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        remoteSource.apply {
            coEvery { getConfigData() } returns remoteConfig
            coEvery { clear() } just Runs
        }
        localSource.apply {
            coEvery { getConfigData() } returns localConfig
            coEvery { clear() } just Runs
        }
        coEvery { defaultSource.getConfigData() } returns defaultConfig

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(1))

        every { cwaSettings.wasDeviceTimeIncorrectAcknowledged } returns false
        every { cwaSettings.wasDeviceTimeIncorrectAcknowledged = any() } just Runs

        every { cwaSettings.firstReliableDeviceTime } returns Instant.EPOCH
        every { cwaSettings.firstReliableDeviceTime = any() } just Runs

        every { cwaSettings.lastDeviceTimeStateChangeAt } returns Instant.EPOCH
        every { cwaSettings.lastDeviceTimeStateChangeAt = any() } just Runs
        every { cwaSettings.lastDeviceTimeStateChangeState } returns ConfigData.DeviceTimeState.INCORRECT
        every { cwaSettings.lastDeviceTimeStateChangeState = any() } just Runs
    }

    private fun createInstance() = AppConfigSource(
        remoteAppConfigSource = remoteSource,
        localAppConfigSource = localSource,
        defaultAppConfigSource = defaultSource,
        timeStamper = timeStamper,
        cwaSettings = cwaSettings
    )

    @Test
    fun `local config is used if available and valid`() = runTest {
        val instance = createInstance()
        instance.getConfigData() shouldBe localConfig

        coVerifySequence {
            localSource.getConfigData()
            timeStamper.nowUTC
        }
    }

    @Test
    fun `remote config is used if local config is not valid`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH
            .plus(Duration.standardHours(1))
            .plus(Duration.standardSeconds(301)) // Local config has 300 seconds validity

        val instance = createInstance()
        instance.getConfigData() shouldBe remoteConfig

        coVerifyOrder {
            localSource.getConfigData()
            timeStamper.nowUTC
            remoteSource.getConfigData()
        }
    }

    @Test
    fun `local config is used despite being invalid if remote config is unavailable`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(2))
        coEvery { remoteSource.getConfigData() } returns null

        val instance = createInstance()
        // The fallback "local" config has a forced offset of zero as we we want isDeviceTimeCorrect=true
        instance.getConfigData() shouldBe localConfig.copy(
            localOffset = Duration.ZERO
        )

        coVerifySequence {
            localSource.getConfigData()
            timeStamper.nowUTC
            remoteSource.getConfigData()
        }
    }

    @Test
    fun `default config is used if remote and local are unavailable`() = runTest {
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

    @Test
    fun `remote config with correct device time resets user acknowledgement`() = runTest {
        coEvery { localSource.getConfigData() } returns null
        every { cwaSettings.wasDeviceTimeIncorrectAcknowledged } returns true

        createInstance().getConfigData()

        coVerifyOrder {
            localSource.getConfigData()
            remoteSource.getConfigData()
            cwaSettings.wasDeviceTimeIncorrectAcknowledged
            cwaSettings.wasDeviceTimeIncorrectAcknowledged = false
        }
    }

    @Test
    fun `remote config with incorrect device time does not reset user acknowledgement`() = runTest {
        coEvery { localSource.getConfigData() } returns null

        coEvery { remoteSource.getConfigData() } returns remoteConfig.copy(
            localOffset = Duration.standardHours(3)
        )

        createInstance().getConfigData()

        coVerifySequence {
            localSource.getConfigData()
            remoteSource.getConfigData()
        }
    }

    @Test
    fun `first reliable device time is set when the remote config has the correct device time`() = runTest {
        coEvery { localSource.getConfigData() } returns null

        createInstance().getConfigData()

        verify {
            cwaSettings.firstReliableDeviceTime = Instant.EPOCH.plus(Duration.standardHours(1))
        }
    }

    @Test
    fun `first reliable device time is not set, if it has already been set`() = runTest {
        coEvery { localSource.getConfigData() } returns null
        every { cwaSettings.firstReliableDeviceTime } returns Instant.ofEpochMilli(1234L)

        createInstance().getConfigData()

        verify(exactly = 0) {
            cwaSettings.firstReliableDeviceTime = any()
        }
    }

    @Test
    fun `first reliable device time is not set, if the device time is incorrect`() = runTest {
        coEvery { remoteSource.getConfigData() } returns remoteConfig.copy(localOffset = Duration.standardDays(1))
        coEvery { localSource.getConfigData() } returns null

        createInstance().getConfigData()

        verify(exactly = 0) {
            cwaSettings.firstReliableDeviceTime = any()
        }
    }

    @Test
    fun `if the device time state changes we save the timestamp and the current state`() = runTest {
        coEvery { localSource.getConfigData() } returns null
        // INCORRECT
        coEvery { remoteSource.getConfigData() } returns remoteConfig.copy(localOffset = Duration.standardDays(1))

        createInstance().getConfigData()

        verify(exactly = 0) {
            cwaSettings.lastDeviceTimeStateChangeAt = any()
            cwaSettings.lastDeviceTimeStateChangeState = any()
        }

        coEvery { remoteSource.getConfigData() } returns remoteConfig

        createInstance().getConfigData()

        verify {
            cwaSettings.lastDeviceTimeStateChangeAt = Instant.EPOCH.plus(Duration.standardHours(1))
            cwaSettings.lastDeviceTimeStateChangeState = ConfigData.DeviceTimeState.CORRECT
        }
    }

    @Test
    fun `clear calls subroutines`() = runTest {
        createInstance().clear()

        coVerify {
            localSource.clear()
            remoteSource.clear()
        }
    }
}
