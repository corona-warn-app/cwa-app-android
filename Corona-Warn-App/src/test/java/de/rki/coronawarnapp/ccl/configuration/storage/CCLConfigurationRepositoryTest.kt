package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.common.CCLConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CCLConfigurationServer
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.called
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

    @RelaxedMockK lateinit var cclConfigurationStorage: CCLConfigurationStorage
    @MockK lateinit var defaultCCLConfigurationProvider: DefaultCCLConfigurationProvider
    @MockK lateinit var cclConfigurationParser: CCLConfigurationParser
    @MockK lateinit var cclConfigurationServer: CCLConfigurationServer

    private val defaultCCLConfig: CCLConfiguration = mockk { every { identifier } returns "defaultCCLConfig" }
    private val storageCCLConfig: CCLConfiguration = mockk { every { identifier } returns "storageCCLConfig" }
    private val serverCCLConfig: CCLConfiguration = mockk { every { identifier } returns "serverCCLConfig" }

    private val defaultCCLConfigRaw = defaultCCLConfig.identifier.toByteArray()
    private val storageCCLConfigRaw = storageCCLConfig.identifier.toByteArray()
    private val serverCCLConfigRaw = serverCCLConfig.identifier.toByteArray()

    private fun createInstance(scope: CoroutineScope): CCLConfigurationRepository = CCLConfigurationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        cclConfigurationStorage = cclConfigurationStorage,
        defaultCCLConfigurationProvider = defaultCCLConfigurationProvider,
        cclConfigurationParser = cclConfigurationParser,
        cclConfigurationServer = cclConfigurationServer
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData() } returns defaultCCLConfigRaw
        coEvery { cclConfigurationStorage.load() } returns storageCCLConfigRaw
        coEvery { cclConfigurationServer.getCCLConfiguration() } returns serverCCLConfigRaw

        every { cclConfigurationParser.parseCClConfigurations(defaultCCLConfigRaw) } returns listOf(defaultCCLConfig)
        every { cclConfigurationParser.parseCClConfigurations(storageCCLConfigRaw) } returns listOf(storageCCLConfig)
        every { cclConfigurationParser.parseCClConfigurations(serverCCLConfigRaw) } returns listOf(serverCCLConfig)
    }

    @Test
    fun `loads default ccl config if storage is empty`() = runBlockingTest2(ignoreActive = true) {
        coEvery { cclConfigurationStorage.load() } returns null

        createInstance(scope = this).cclConfigurations.first().first() shouldBe defaultCCLConfig

        coVerify {
            cclConfigurationStorage.load()
            defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigRaw)
        }
    }

    @Test
    fun `loads ccl config from storage`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).cclConfigurations.first().first() shouldBe storageCCLConfig

        coVerify {
            defaultCCLConfigurationProvider wasNot called
            cclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
        }
    }

    @Test
    fun `clear calls storage clear`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).clear()

        coVerify {
            cclConfigurationStorage.clear()
        }
    }

    @Test
    fun `provides default ccl config after clearing`() = runBlockingTest2(ignoreActive = true) {
        coEvery { cclConfigurationStorage.load() } returns storageCCLConfigRaw andThen null

        with(createInstance(scope = this)) {
            cclConfigurations.first().first() shouldBe storageCCLConfig
            clear()
            cclConfigurations.first().first() shouldBe defaultCCLConfig
        }

        coVerifyOrder {
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
            cclConfigurationStorage.clear()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigRaw)
        }
    }
}
