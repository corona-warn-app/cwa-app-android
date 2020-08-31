package de.rki.coronawarnapp.service.applicationconfiguration

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.util.CWADebug
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

        mockkObject(WebRequestBuilder)
        val requestBuilder = mockk<WebRequestBuilder>()
        val appConfig = mockk<ApplicationConfigurationOuterClass.ApplicationConfiguration>()
        val appConfigBuilder =
            mockk<ApplicationConfigurationOuterClass.ApplicationConfiguration.Builder>()

        every { appConfig.toBuilder() } returns appConfigBuilder

        every { appConfigBuilder.addAllSupportedCountries(any()) } returns appConfigBuilder

        every { appConfigBuilder.clearSupportedCountries() } returns appConfigBuilder

        every { appConfigBuilder.build() } returns appConfig

        coEvery { requestBuilder.asyncGetApplicationConfigurationFromServer() } returns appConfig

        every { WebRequestBuilder.getInstance() } returns requestBuilder

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
