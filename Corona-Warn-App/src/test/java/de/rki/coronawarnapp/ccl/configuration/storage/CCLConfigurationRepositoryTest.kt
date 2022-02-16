package de.rki.coronawarnapp.ccl.configuration.storage

import com.upokecenter.cbor.CBORException
import de.rki.coronawarnapp.ccl.configuration.common.CCLConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CCLConfigurationServer
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
import testhelpers.coroutines.runBlockingTest2

class CCLConfigurationRepositoryTest : BaseTest() {

    @RelaxedMockK lateinit var downloadedCclConfigurationStorage: DownloadedCCLConfigurationStorage
    @MockK lateinit var defaultCCLConfigurationProvider: DefaultCCLConfigurationProvider
    @MockK lateinit var cclConfigurationParser: CCLConfigurationParser
    @MockK lateinit var cclConfigurationServer: CCLConfigurationServer

    private val defaultCCLConfig: CCLConfiguration = mockk {
        every { identifier } returns "defaultCCLConfig"
        every { version } returns "1"
    }
    private val storageCCLConfig: CCLConfiguration = mockk {
        every { identifier } returns "storageCCLConfig"
        every { version } returns "1"
    }
    private val serverCCLConfig: CCLConfiguration = mockk {
        every { identifier } returns "serverCCLConfig"
        every { version } returns "1"
    }

    private val defaultCCLConfigRaw = defaultCCLConfig.identifier.toByteArray()
    private val storageCCLConfigRaw = storageCCLConfig.identifier.toByteArray()
    private val serverCCLConfigRaw = serverCCLConfig.identifier.toByteArray()

    private fun createInstance(scope: CoroutineScope): CCLConfigurationRepository = CCLConfigurationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        downloadedCclConfigurationStorage = downloadedCclConfigurationStorage,
        defaultCCLConfigurationProvider = defaultCCLConfigurationProvider,
        cclConfigurationParser = cclConfigurationParser,
        cclConfigurationServer = cclConfigurationServer,
        cclConfigurationMerger = CCLConfigurationMerger()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData() } returns defaultCCLConfigRaw
        coEvery { downloadedCclConfigurationStorage.load() } returns storageCCLConfigRaw
        coEvery { cclConfigurationServer.getCCLConfiguration() } returns serverCCLConfigRaw

        every { cclConfigurationParser.parseCClConfigurations(defaultCCLConfigRaw) } returns listOf(defaultCCLConfig)
        every { cclConfigurationParser.parseCClConfigurations(storageCCLConfigRaw) } returns listOf(storageCCLConfig)
        every { cclConfigurationParser.parseCClConfigurations(serverCCLConfigRaw) } returns listOf(serverCCLConfig)
    }

    @Test
    fun `loads default ccl config if storage is empty`() = runBlockingTest2(ignoreActive = true) {
        coEvery { downloadedCclConfigurationStorage.load() } returns null

        createInstance(scope = this).getCCLConfigurations() shouldBe listOf(defaultCCLConfig)

        coVerify {
            downloadedCclConfigurationStorage.load()
            defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigRaw)
        }
    }

    @Test
    fun `loads and merges ccl config from storage`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).getCCLConfigurations().run {
            // merged config
            size shouldBe 2
            contains(defaultCCLConfig) shouldBe true
            contains(storageCCLConfig) shouldBe true
        }

        coVerify {
            downloadedCclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
        }
    }

    @Test
    fun `clear calls storage clear`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).clear()

        coVerify {
            downloadedCclConfigurationStorage.clear()
        }
    }

    @Test
    fun `provides default ccl config after clearing`() = runBlockingTest2(ignoreActive = true) {
        coEvery { downloadedCclConfigurationStorage.load() } returns storageCCLConfigRaw andThen null

        with(createInstance(scope = this)) {
            getCCLConfigurations().run {
                // merged config
                size shouldBe 2
                contains(storageCCLConfig) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }
            clear()
            getCCLConfigurations() shouldBe listOf(defaultCCLConfig)
        }

        coVerifyOrder {
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
            downloadedCclConfigurationStorage.clear()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigRaw)
        }
    }

    @Test
    fun `getCCLConfigurations returns the actual item of cclConfigurations`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).run {
            getCCLConfigurations() shouldBe cclConfigurations.first()
            clear()
            getCCLConfigurations() shouldBe cclConfigurations.first()
            updateCCLConfiguration() shouldBe UpdateResult.UPDATE
            getCCLConfigurations() shouldBe cclConfigurations.first()
        }
    }

    @Test
    fun `update returns true only if ccl config was updated`() = runBlockingTest2(ignoreActive = true) {
        val serverCCLConfig2: CCLConfiguration = mockk {
            every { identifier } returns "serverCCLConfig2"
            every { version } returns "1"
        }
        val serverCCLConfig2Raw = serverCCLConfig2.identifier.toByteArray()

        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfig2Raw) } returns listOf(
            serverCCLConfig2
        )
        coEvery {
            cclConfigurationServer.getCCLConfiguration()
        } returns serverCCLConfigRaw andThen null andThen serverCCLConfig2Raw

        createInstance(scope = this).run {
            getCCLConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(storageCCLConfig) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }

            updateCCLConfiguration() shouldBe UpdateResult.UPDATE
            getCCLConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(serverCCLConfig) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }

            updateCCLConfiguration() shouldBe UpdateResult.NO_UPDATE
            getCCLConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(serverCCLConfig) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }

            updateCCLConfiguration() shouldBe UpdateResult.UPDATE
            getCCLConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(serverCCLConfig2) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }
        }

        coVerify {
            downloadedCclConfigurationStorage.save(rawData = serverCCLConfigRaw)
            downloadedCclConfigurationStorage.save(rawData = serverCCLConfig2Raw)
        }
    }

    @Test
    fun `faulty server data will not ruin existing data`() = runBlockingTest2(ignoreActive = true) {
        val error = CBORException("Test error")
        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw) } throws error

        createInstance(scope = this).run {
            getCCLConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(storageCCLConfig) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }

            updateCCLConfiguration() shouldBe UpdateResult.FAIL
            getCCLConfigurations().run {
                // Merged configs
                size shouldBe 2
                contains(storageCCLConfig) shouldBe true
                contains(defaultCCLConfig) shouldBe true
            }
        }

        coVerify {
            cclConfigurationServer.getCCLConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw)
        }

        coVerify(exactly = 0) {
            downloadedCclConfigurationStorage.save(rawData = any())
        }
    }
}
