package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CCLConfigurationMergerTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `merge() should properly merge default and downloaded ccl configurations`() {
        val defaultConfiguration1 = mockk<CCLConfiguration> { every { identifier } returns "CCL-DE-0001" }
        val defaultConfiguration2 = mockk<CCLConfiguration> { every { identifier } returns "CCL-DE-0002" }
        val defaultConfiguration = listOf(defaultConfiguration1, defaultConfiguration2)

        val downloadedConfiguration1 = mockk<CCLConfiguration> { every { identifier } returns "CCL-DE-0001" }
        val downloadedConfiguration = listOf(downloadedConfiguration1)

        CCLConfigurationMerger().merge(defaultConfiguration, downloadedConfiguration) shouldBe listOf(
            downloadedConfiguration1,
            defaultConfiguration2
        )
    }
}
