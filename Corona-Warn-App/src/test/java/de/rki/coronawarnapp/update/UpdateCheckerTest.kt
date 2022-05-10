package de.rki.coronawarnapp.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.environment.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.IOException

class UpdateCheckerTest : BaseTest() {

    @MockK private lateinit var configData: ConfigData
    @MockK private lateinit var cachedConfigData: ConfigData
    @MockK private lateinit var appConfigProvider: AppConfigProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(BuildConfigWrap)

        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)
    }

    fun createInstance() = UpdateChecker(
        appConfigProvider = appConfigProvider
    )

    @Test
    fun `update is required`() = runTest {
        every { configData.minVersionCode } returns 10
        every { BuildConfigWrap.VERSION_CODE } returns 9

        coEvery { appConfigProvider.getAppConfig() } returns configData

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe true
        }

        coVerifySequence {
            appConfigProvider.currentConfig
            BuildConfigWrap.VERSION_CODE
            appConfigProvider.getAppConfig()
            BuildConfigWrap.VERSION_CODE
        }
    }

    @Test
    fun `update is NOT required`() = runTest {
        every { configData.minVersionCode } returns 10
        every { BuildConfigWrap.VERSION_CODE } returns 11

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe false
        }

        coVerifySequence {
            appConfigProvider.currentConfig
            BuildConfigWrap.VERSION_CODE
        }

        coVerify(exactly = 0) {
            appConfigProvider.getAppConfig()
        }
    }

    @Test
    fun `general error defaults to no update required`() = runTest {
        every { configData.minVersionCode } throws Exception()

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe false
        }
    }

    @Test
    fun `config parsing error means update is required`() = runTest {
        every { configData.minVersionCode } throws ApplicationConfigurationCorruptException()

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe true
        }

        coVerifySequence {
            appConfigProvider.currentConfig
        }
    }

    @Test
    fun `timeout after 5 seconds - cached needs update`() = runTest {
        every { configData.minVersionCode } returns 10
        every { BuildConfigWrap.VERSION_CODE } returns 9

        coEvery { appConfigProvider.getAppConfig() } coAnswers {
            delay(4_000)
            configData
        }

        every { appConfigProvider.currentConfig } returns flowOf(
            cachedConfigData.apply {
                every { minVersionCode } returns 10
            }
        )

        createInstance().assertUpdateIsNeeded() shouldBe true

        coEvery { appConfigProvider.getAppConfig() } coAnswers {
            delay(6_000)
            throw IOException()
        }

        createInstance().assertUpdateIsNeeded() shouldBe true

        coEvery { appConfigProvider.getAppConfig() } coAnswers {
            delay(6_000)
            configData
        }

        createInstance().assertUpdateIsNeeded() shouldBe true
    }

    @Test
    fun `timeout after 5 seconds - cached does not need update`() = runTest {
        every { configData.minVersionCode } returns 10
        every { BuildConfigWrap.VERSION_CODE } returns 9

        coEvery { appConfigProvider.getAppConfig() } coAnswers {
            delay(4_000)
            configData
        }

        every { appConfigProvider.currentConfig } returns flowOf(
            cachedConfigData.apply {
                every { minVersionCode } returns 9
            }
        )

        createInstance().assertUpdateIsNeeded() shouldBe true

        coEvery { appConfigProvider.getAppConfig() } coAnswers {
            delay(6_000)
            throw IOException()
        }

        createInstance().assertUpdateIsNeeded() shouldBe false

        coEvery { appConfigProvider.getAppConfig() } coAnswers {
            delay(6_000)
            configData
        }

        createInstance().assertUpdateIsNeeded() shouldBe false
    }
}
