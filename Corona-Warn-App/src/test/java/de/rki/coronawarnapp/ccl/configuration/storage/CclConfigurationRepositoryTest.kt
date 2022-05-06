package de.rki.coronawarnapp.ccl.configuration.storage

import com.upokecenter.cbor.CBORException
import de.rki.coronawarnapp.ccl.configuration.common.CclConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CclConfigurationServer
import de.rki.coronawarnapp.util.repositories.UpdateResult
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2

class CclConfigurationRepositoryTest : BaseTest() {

    @RelaxedMockK lateinit var downloadedCclConfigurationStorage: DownloadedCclConfigurationStorage
    @MockK lateinit var defaultCclConfigurationProvider: DefaultCclConfigurationProvider
    @MockK lateinit var cclConfigurationParser: CclConfigurationParser
    @MockK lateinit var cclConfigurationServer: CclConfigurationServer

    private val defaultCclConfig: CclConfiguration = mockk {
        every { identifier } returns "defaultCCLConfig"
        every { version } returns "1"
    }
    private val storageCclConfig: CclConfiguration = mockk {
        every { identifier } returns "storageCCLConfig"
        every { version } returns "1"
    }
    private val serverCclConfig: CclConfiguration = mockk {
        every { identifier } returns "serverCCLConfig"
        every { version } returns "1"
    }

    private val defaultCclConfigRaw = defaultCclConfig.identifier.toByteArray()
    private val storageCclConfigRaw = storageCclConfig.identifier.toByteArray()
    private val serverCclConfigRaw = serverCclConfig.identifier.toByteArray()

    private fun createInstance(scope: CoroutineScope): CclConfigurationRepository = CclConfigurationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        downloadedCclConfigurationStorage = downloadedCclConfigurationStorage,
        defaultCclConfigurationProvider = defaultCclConfigurationProvider,
        cclConfigurationParser = cclConfigurationParser,
        cclConfigurationServer = cclConfigurationServer,
        cclConfigurationMerger = CclConfigurationMerger()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { defaultCclConfigurationProvider.loadDefaultCclConfigurationsRawData() } returns defaultCclConfigRaw
        coEvery { downloadedCclConfigurationStorage.load() } returns storageCclConfigRaw
        coEvery { cclConfigurationServer.getCclConfiguration() } returns serverCclConfigRaw

        every { cclConfigurationParser.parseCClConfigurations(defaultCclConfigRaw) } returns listOf(defaultCclConfig)
        every { cclConfigurationParser.parseCClConfigurations(storageCclConfigRaw) } returns listOf(storageCclConfig)
        every { cclConfigurationParser.parseCClConfigurations(serverCclConfigRaw) } returns listOf(serverCclConfig)
    }

    @Test
    fun `loads default ccl config if storage is empty`() = runTest2 {
        coEvery { downloadedCclConfigurationStorage.load() } returns null

        createInstance(scope = this).getCclConfigurations() shouldBe listOf(defaultCclConfig)

        coVerify {
            downloadedCclConfigurationStorage.load()
            defaultCclConfigurationProvider.loadDefaultCclConfigurationsRawData()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCclConfigRaw)
        }
    }

    @Test
    fun `loads and merges ccl config from storage`() = runTest2 {
        createInstance(scope = this).getCclConfigurations().run {
            // merged config
            size shouldBe 2
            contains(defaultCclConfig) shouldBe true
            contains(storageCclConfig) shouldBe true
        }

        coVerify {
            downloadedCclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCclConfigRaw)
        }
    }

    @Test
    fun `clear calls storage clear`() = runTest2 {
        createInstance(scope = this).clear()

        coVerify {
            downloadedCclConfigurationStorage.clear()
        }
    }

    @Test
    fun `provides default ccl config after clearing`() = runTest2 {
        coEvery { downloadedCclConfigurationStorage.load() } returns storageCclConfigRaw andThen null

        with(createInstance(scope = this)) {
            getCclConfigurations().run {
                // merged config
                size shouldBe 2
                contains(storageCclConfig) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }
            clear()
            getCclConfigurations() shouldBe listOf(defaultCclConfig)
        }

        coVerifyOrder {
            cclConfigurationParser.parseCClConfigurations(rawData = storageCclConfigRaw)
            downloadedCclConfigurationStorage.clear()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCclConfigRaw)
        }
    }

    @Test
    fun `getCclConfigurations returns the actual item of cclConfigurations`() = runTest2 {
        createInstance(scope = this).run {
            getCclConfigurations() shouldBe cclConfigurations.first()
            clear()
            getCclConfigurations() shouldBe cclConfigurations.first()
            updateCclConfiguration() shouldBe UpdateResult.UPDATE
            getCclConfigurations() shouldBe cclConfigurations.first()
        }
    }

    @Test
    fun `update returns true only if ccl config was updated`() = runTest2 {
        val serverCclConfig2: CclConfiguration = mockk {
            every { identifier } returns "serverCCLConfig2"
            every { version } returns "1"
        }
        val serverCclConfig2Raw = serverCclConfig2.identifier.toByteArray()

        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfig2Raw) } returns listOf(
            serverCclConfig2
        )
        coEvery {
            cclConfigurationServer.getCclConfiguration()
        } returns serverCclConfigRaw andThen null andThen serverCclConfig2Raw

        createInstance(scope = this).run {
            getCclConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(storageCclConfig) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }

            updateCclConfiguration() shouldBe UpdateResult.UPDATE
            getCclConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(serverCclConfig) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }

            updateCclConfiguration() shouldBe UpdateResult.NO_UPDATE
            getCclConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(serverCclConfig) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }

            updateCclConfiguration() shouldBe UpdateResult.UPDATE
            getCclConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(serverCclConfig2) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }
        }

        coVerify {
            downloadedCclConfigurationStorage.save(rawData = serverCclConfigRaw)
            downloadedCclConfigurationStorage.save(rawData = serverCclConfig2Raw)
        }
    }

    @Test
    fun `faulty server data will not ruin existing data`() = runTest2 {
        val error = CBORException("Test error")
        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw) } throws error

        createInstance(scope = this).run {
            getCclConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(storageCclConfig) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }

            updateCclConfiguration() shouldBe UpdateResult.FAIL
            getCclConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(storageCclConfig) shouldBe true
                contains(defaultCclConfig) shouldBe true
            }
        }

        coVerify {
            cclConfigurationServer.getCclConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw)
        }

        coVerify(exactly = 0) {
            downloadedCclConfigurationStorage.save(rawData = any())
        }
    }
}
