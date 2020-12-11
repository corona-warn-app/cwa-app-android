package de.rki.coronawarnapp.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.environment.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class UpdateCheckerTest : BaseTest() {

    @MockK private lateinit var configData: ConfigData
    @MockK private lateinit var appConfigProvider: AppConfigProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(BuildConfigWrap)

        coEvery { appConfigProvider.getAppConfig() } returns configData
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    fun createInstance() = UpdateChecker(
        appConfigProvider = appConfigProvider
    )

    @Test
    fun `update is required`() = runBlockingTest {
        every { configData.minVersionCode } returns 10
        every { BuildConfigWrap.VERSION_CODE } returns 9

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe true
            updateIntent shouldNotBe null
        }

        coVerifySequence {
            appConfigProvider.getAppConfig()
            BuildConfigWrap.VERSION_CODE
        }
    }

    @Test
    fun `update is NOT required`() = runBlockingTest {
        every { configData.minVersionCode } returns 10
        every { BuildConfigWrap.VERSION_CODE } returns 11

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe false
            updateIntent shouldBe null
        }

        coVerifySequence {
            appConfigProvider.getAppConfig()
            BuildConfigWrap.VERSION_CODE
        }
    }

    @Test
    fun `general error defaults to no update required`() = runBlockingTest {
        every { configData.minVersionCode } throws Exception()

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe false
            updateIntent shouldBe null
        }
    }

    @Test
    fun `config parsing error means update is required`() = runBlockingTest {
        every { configData.minVersionCode } throws ApplicationConfigurationCorruptException()

        createInstance().checkForUpdate().apply {
            isUpdateNeeded shouldBe true
            updateIntent shouldNotBe null
        }

        coVerifySequence {
            appConfigProvider.getAppConfig()
        }
    }
}
