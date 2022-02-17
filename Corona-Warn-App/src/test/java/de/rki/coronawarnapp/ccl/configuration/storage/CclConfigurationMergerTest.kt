package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class CclConfigurationMergerTest {

    @Test
    fun `merge() should properly merge default and downloaded ccl configurations`() {
        val defaultConfiguration1 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0001" }
        val defaultConfiguration2 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0002" }
        val defaultConfiguration = listOf(defaultConfiguration1, defaultConfiguration2)

        val downloadedConfiguration1 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0001" }
        val downloadedConfiguration2 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0003" }
        val downloadedConfiguration = listOf(downloadedConfiguration1, downloadedConfiguration2)

        val mergedConfig = CclConfigurationMerger().merge(defaultConfiguration, downloadedConfiguration)

        with(mergedConfig) {
            size shouldBe 3

            contains(downloadedConfiguration1) shouldBe true
            contains(downloadedConfiguration2) shouldBe true
            contains(defaultConfiguration2) shouldBe true

            contains(defaultConfiguration1) shouldBe false
        }
    }

    @Test
    fun `merge should return downloaded configs if default configs are empty`() {
        val downloadedConfiguration1 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0001" }
        val downloadedConfiguration2 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0002" }
        val downloadedConfiguration = listOf(downloadedConfiguration1, downloadedConfiguration2)

        val mergedConfig = CclConfigurationMerger().merge(
            defaultConfigList = emptyList(),
            downloadedConfigList = downloadedConfiguration
        )

        with(mergedConfig) {
            size shouldBe 2

            contains(downloadedConfiguration1) shouldBe true
            contains(downloadedConfiguration2) shouldBe true
        }
    }

    @Test
    fun `merge should return default configs if downloaded configs are empty`() {
        val defaultConfiguration1 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0001" }
        val defaultConfiguration2 = mockk<CclConfiguration> { every { identifier } returns "CCL-DE-0002" }
        val defaultConfiguration = listOf(defaultConfiguration1, defaultConfiguration2)

        val mergedConfig = CclConfigurationMerger().merge(
            defaultConfigList = defaultConfiguration,
            downloadedConfigList = emptyList()
        )

        with(mergedConfig) {
            size shouldBe 2

            contains(defaultConfiguration1) shouldBe true
            contains(defaultConfiguration2) shouldBe true
        }
    }
}
