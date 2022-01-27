package de.rki.coronawarnapp.ccl.configuration.update

import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
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
    @MockK private lateinit var boosterRulesRepository: BoosterRulesRepository

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

        // no update required on the same day
        val sameDay = Instant.parse("2000-01-01T23:59:59Z")
        updater.isUpdateRequired(sameDay) shouldBe false

        // update required on next day
        val nextDay = Instant.parse("2000-01-02T00:00:00Z")
        updater.isUpdateRequired(nextDay) shouldBe true

        // update should also happen on previous day (can happen when user fumbles with the device date)
        val previousDay = Instant.parse("1999-12-31T00:00:00Z")
        updater.isUpdateRequired(previousDay)
    }

    private fun getInstance(): CCLConfigurationUpdater {
        return CCLConfigurationUpdater(cclSettings, boosterRulesRepository)
    }
}
