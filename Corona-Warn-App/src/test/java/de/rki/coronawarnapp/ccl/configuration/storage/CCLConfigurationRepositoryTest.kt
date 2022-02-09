package de.rki.coronawarnapp.ccl.configuration.storage

import com.upokecenter.cbor.CBORException
import de.rki.coronawarnapp.ccl.configuration.common.CCLConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CCLConfigurationServer
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository.UpdateResult.FAIL
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository.UpdateResult.NO_UPDATE
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository.UpdateResult.UPDATE
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

        createInstance(scope = this).getCCLConfigurations().first() shouldBe defaultCCLConfig

        coVerify {
            cclConfigurationStorage.load()
            defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigRaw)
        }
    }

    @Test
    fun `loads ccl config from storage`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).getCCLConfigurations().first() shouldBe storageCCLConfig

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
            getCCLConfigurations().first() shouldBe storageCCLConfig
            clear()
            getCCLConfigurations().first() shouldBe defaultCCLConfig
        }

        coVerifyOrder {
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
            cclConfigurationStorage.clear()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigRaw)
        }
    }

    @Test
    fun `getCCLConfigurations returns the actual item of cclConfigurations`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).run {
            getCCLConfigurations() shouldBe cclConfigurations.first()
            clear()
            getCCLConfigurations() shouldBe cclConfigurations.first()
            updateCCLConfiguration() shouldBe UPDATE
            getCCLConfigurations() shouldBe cclConfigurations.first()
        }
    }

    @Test
    fun `update returns true only if ccl config was updated`() = runBlockingTest2(ignoreActive = true) {
        val serverCCLConfig2: CCLConfiguration = mockk { every { identifier } returns "serverCCLConfig2" }
        val serverCCLConfig2Raw = serverCCLConfig2.identifier.toByteArray()

        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfig2Raw) } returns listOf(
            serverCCLConfig2
        )
        coEvery {
            cclConfigurationServer.getCCLConfiguration()
        } returns serverCCLConfigRaw andThen serverCCLConfigRaw andThen serverCCLConfig2Raw

        createInstance(scope = this).run {
            getCCLConfigurations().first() shouldBe storageCCLConfig
            updateCCLConfiguration() shouldBe UPDATE
            getCCLConfigurations().first() shouldBe serverCCLConfig
            updateCCLConfiguration() shouldBe NO_UPDATE
            getCCLConfigurations().first() shouldBe serverCCLConfig
            updateCCLConfiguration() shouldBe UPDATE
            getCCLConfigurations().first() shouldBe serverCCLConfig2
        }

        coVerifyOrder {
            cclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
            cclConfigurationServer.getCCLConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw)
            cclConfigurationStorage.save(rawData = serverCCLConfigRaw)
            cclConfigurationServer.getCCLConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw)
            cclConfigurationServer.getCCLConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfig2Raw)
            cclConfigurationStorage.save(rawData = serverCCLConfig2Raw)
        }
    }

    @Test
    fun `update returns false if ccl config was not updated`() = runBlockingTest2(ignoreActive = true) {
        coEvery { cclConfigurationServer.getCCLConfiguration() } returns null

        createInstance(scope = this).run {
            getCCLConfigurations().first() shouldBe storageCCLConfig
            updateCCLConfiguration() shouldBe NO_UPDATE
            getCCLConfigurations().first() shouldBe storageCCLConfig
        }

        coVerifyOrder {
            cclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCCLConfigRaw)
            cclConfigurationServer.getCCLConfiguration()
        }

        coVerify(exactly = 0) {
            cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw)
            cclConfigurationStorage.save(rawData = serverCCLConfigRaw)
        }
    }

    @Test
    fun `faulty server data will not ruin existing data`() = runBlockingTest2(ignoreActive = true) {
        val error = CBORException("Test error")
        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw) } throws error

        createInstance(scope = this).run {
            getCCLConfigurations().first() shouldBe storageCCLConfig
            updateCCLConfiguration() shouldBe FAIL
            getCCLConfigurations().first() shouldBe storageCCLConfig
        }

        coVerify {
            cclConfigurationServer.getCCLConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCCLConfigRaw)
        }

        coVerify(exactly = 0) {
            cclConfigurationStorage.save(rawData = any())
        }
    }
}
