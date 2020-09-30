package de.rki.coronawarnapp.service.applicationconfiguration

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import testhelpers.BaseTest

class ApplicationConfigurationServiceTest : BaseTest() {

    @Test
    fun `mock country codes only in debug`() {
        mockkObject(CWADebug)
        every { CWADebug.isDebugBuildOrMode } returns true

        CWADebug.isDebugBuildOrMode shouldBe true

        val appConfig = mockk<ApplicationConfigurationOuterClass.ApplicationConfiguration>()
        val appConfigBuilder =
            mockk<ApplicationConfigurationOuterClass.ApplicationConfiguration.Builder>()

        every { appConfig.toBuilder() } returns appConfigBuilder

        every { appConfigBuilder.addAllSupportedCountries(any()) } returns appConfigBuilder

        every { appConfigBuilder.clearSupportedCountries() } returns appConfigBuilder

        every { appConfigBuilder.build() } returns appConfig

        val downloadServer = mockk<AppConfigProvider>()
        coEvery { downloadServer.getAppConfig() } returns appConfig

        mockkObject(AppInjector)
        mockk<ApplicationComponent>().apply {
            every { this@apply.appConfigProvider } returns downloadServer
            every { AppInjector.component } returns this@apply
        }

        runBlocking {
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
            verify(exactly = 1) { appConfigBuilder.addAllSupportedCountries(any()) }
        }

        every { CWADebug.isDebugBuildOrMode } returns false

        CWADebug.isDebugBuildOrMode shouldBe false

        runBlocking {
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
            verify(exactly = 1) { appConfigBuilder.addAllSupportedCountries(any()) }
        }
    }
}
