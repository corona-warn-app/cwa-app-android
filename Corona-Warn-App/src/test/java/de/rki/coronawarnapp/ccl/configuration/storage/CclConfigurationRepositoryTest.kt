package de.rki.coronawarnapp.ccl.configuration.storage

import com.upokecenter.cbor.CBORException
import de.rki.coronawarnapp.ccl.configuration.common.CclConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CclConfigurationServer
import de.rki.coronawarnapp.util.repositories.UpdateResult
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

class CclConfigurationRepositoryTest : BaseTest() {

    @RelaxedMockK lateinit var cclConfigurationStorage: CclConfigurationStorage
    @MockK lateinit var defaultCclConfigurationProvider: DefaultCclConfigurationProvider
    @MockK lateinit var cclConfigurationParser: CclConfigurationParser
    @MockK lateinit var cclConfigurationServer: CclConfigurationServer

    private val defaultCclConfig: CclConfiguration = mockk { every { identifier } returns "defaultCCLConfig" }
    private val storageCclConfig: CclConfiguration = mockk { every { identifier } returns "storageCCLConfig" }
    private val serverCclConfig: CclConfiguration = mockk { every { identifier } returns "serverCCLConfig" }

    private val defaultCclConfigRaw = defaultCclConfig.identifier.toByteArray()
    private val storageCclConfigRaw = storageCclConfig.identifier.toByteArray()
    private val serverCclConfigRaw = serverCclConfig.identifier.toByteArray()

    private fun createInstance(scope: CoroutineScope): CclConfigurationRepository = CclConfigurationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        cclConfigurationStorage = cclConfigurationStorage,
        defaultCclConfigurationProvider = defaultCclConfigurationProvider,
        cclConfigurationParser = cclConfigurationParser,
        cclConfigurationServer = cclConfigurationServer
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { defaultCclConfigurationProvider.loadDefaultCclConfigurationsRawData() } returns defaultCclConfigRaw
        coEvery { cclConfigurationStorage.load() } returns storageCclConfigRaw
        coEvery { cclConfigurationServer.getCclConfiguration() } returns serverCclConfigRaw

        every { cclConfigurationParser.parseCClConfigurations(defaultCclConfigRaw) } returns listOf(defaultCclConfig)
        every { cclConfigurationParser.parseCClConfigurations(storageCclConfigRaw) } returns listOf(storageCclConfig)
        every { cclConfigurationParser.parseCClConfigurations(serverCclConfigRaw) } returns listOf(serverCclConfig)
    }

    @Test
    fun `loads default ccl config if storage is empty`() = runBlockingTest2(ignoreActive = true) {
        coEvery { cclConfigurationStorage.load() } returns null

        createInstance(scope = this).getCclConfigurations().first() shouldBe defaultCclConfig

        coVerify {
            cclConfigurationStorage.load()
            defaultCclConfigurationProvider.loadDefaultCclConfigurationsRawData()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCclConfigRaw)
        }
    }

    @Test
    fun `loads ccl config from storage`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).getCclConfigurations().first() shouldBe storageCclConfig

        coVerify {
            defaultCclConfigurationProvider wasNot called
            cclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCclConfigRaw)
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
        coEvery { cclConfigurationStorage.load() } returns storageCclConfigRaw andThen null

        with(createInstance(scope = this)) {
            getCclConfigurations().first() shouldBe storageCclConfig
            clear()
            getCclConfigurations().first() shouldBe defaultCclConfig
        }

        coVerifyOrder {
            cclConfigurationParser.parseCClConfigurations(rawData = storageCclConfigRaw)
            cclConfigurationStorage.clear()
            cclConfigurationParser.parseCClConfigurations(rawData = defaultCclConfigRaw)
        }
    }

    @Test
    fun `getCCLConfigurations returns the actual item of cclConfigurations`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).run {
            getCclConfigurations() shouldBe cclConfigurations.first()
            clear()
            getCclConfigurations() shouldBe cclConfigurations.first()
            updateCclConfiguration() shouldBe UpdateResult.UPDATE
            getCclConfigurations() shouldBe cclConfigurations.first()
        }
    }

    @Test
    fun `update returns true only if ccl config was updated`() = runBlockingTest2(ignoreActive = true) {
        val serverCclConfig2: CclConfiguration = mockk { every { identifier } returns "serverCCLConfig2" }
        val serverCclConfig2Raw = serverCclConfig2.identifier.toByteArray()

        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfig2Raw) } returns listOf(
            serverCclConfig2
        )
        coEvery {
            cclConfigurationServer.getCclConfiguration()
        } returns serverCclConfigRaw andThen serverCclConfigRaw andThen serverCclConfig2Raw

        createInstance(scope = this).run {
            getCclConfigurations().first() shouldBe storageCclConfig
            updateCclConfiguration() shouldBe UpdateResult.UPDATE
            getCclConfigurations().first() shouldBe serverCclConfig
            updateCclConfiguration() shouldBe UpdateResult.NO_UPDATE
            getCclConfigurations().first() shouldBe serverCclConfig
            updateCclConfiguration() shouldBe UpdateResult.UPDATE
            getCclConfigurations().first() shouldBe serverCclConfig2
        }

        coVerifyOrder {
            cclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCclConfigRaw)
            cclConfigurationServer.getCclConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw)
            cclConfigurationStorage.save(rawData = serverCclConfigRaw)
            cclConfigurationServer.getCclConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw)
            cclConfigurationServer.getCclConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfig2Raw)
            cclConfigurationStorage.save(rawData = serverCclConfig2Raw)
        }
    }

    @Test
    fun `update returns false if ccl config was not updated`() = runBlockingTest2(ignoreActive = true) {
        coEvery { cclConfigurationServer.getCclConfiguration() } returns null

        createInstance(scope = this).run {
            getCclConfigurations().first() shouldBe storageCclConfig
            updateCclConfiguration() shouldBe UpdateResult.NO_UPDATE
            getCclConfigurations().first() shouldBe storageCclConfig
        }

        coVerifyOrder {
            cclConfigurationStorage.load()
            cclConfigurationParser.parseCClConfigurations(rawData = storageCclConfigRaw)
            cclConfigurationServer.getCclConfiguration()
        }

        coVerify(exactly = 0) {
            cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw)
            cclConfigurationStorage.save(rawData = serverCclConfigRaw)
        }
    }

    @Test
    fun `faulty server data will not ruin existing data`() = runBlockingTest2(ignoreActive = true) {
        val error = CBORException("Test error")
        every { cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw) } throws error

        createInstance(scope = this).run {
            getCclConfigurations().first() shouldBe storageCclConfig
            updateCclConfiguration() shouldBe UpdateResult.FAIL
            getCclConfigurations().first() shouldBe storageCclConfig
        }

        coVerify {
            cclConfigurationServer.getCclConfiguration()
            cclConfigurationParser.parseCClConfigurations(rawData = serverCclConfigRaw)
        }

        coVerify(exactly = 0) {
            cclConfigurationStorage.save(rawData = any())
        }
    }
}
