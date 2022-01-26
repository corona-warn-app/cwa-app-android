package de.rki.coronawarnapp.ccl.configuration.update

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class CCLConfigurationUpdaterTest : BaseTest() {

    @MockK private lateinit var cclSettings: CCLSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `isUpdateRequires() should return true after one day`() = runBlockingTest {

        val updater = getInstance()

        // update is required when none was performed yet
        coEvery { cclSettings.getLastExecutionTime() } returns null
        updater.isUpdateRequired() shouldBe true

        coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
        val after23h59m = Instant.parse("2000-01-01T23:59:59Z")

        updater.isUpdateRequired(after23h59m) shouldBe false

        val after24h = Instant.parse("2000-01-02T00:00:01Z")

        updater.isUpdateRequired(after24h) shouldBe true
    }

    private fun getInstance(): CCLConfigurationUpdater {
        return CCLConfigurationUpdater(cclSettings)
    }
}
