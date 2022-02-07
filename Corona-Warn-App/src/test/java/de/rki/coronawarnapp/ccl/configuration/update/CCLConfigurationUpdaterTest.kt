package de.rki.coronawarnapp.ccl.configuration.update

import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
internal class CCLConfigurationUpdaterTest : BaseTest() {

    @MockK private lateinit var timeStamper: TimeStamper
    @RelaxedMockK private lateinit var cclSettings: CCLSettings
    @MockK private lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK private lateinit var cclConfigurationRepository: CCLConfigurationRepository
    @RelaxedMockK private lateinit var dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `updateIfRequired should update booster rules and ccl configuration if required`() = runBlockingTest {
        coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
        coEvery { timeStamper.nowUTC } returns Instant.parse("2000-01-02T00:00:00Z")

        coEvery { boosterRulesRepository.update() } returns true
        coEvery { cclConfigurationRepository.updateCCLConfiguration() } returns false

        getInstance().updateIfRequired()

        coVerify(exactly = 1) { boosterRulesRepository.update() }
        coVerify(exactly = 1) { cclConfigurationRepository.updateCCLConfiguration() }

        verify(exactly = 1) { cclSettings.setExecutionTimeToNow(any()) }

        verify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdateAfterCertificateChange(true) }

        // false should be passed to the trigger when there are no updates
        coEvery { boosterRulesRepository.update() } returns false
        coEvery { cclConfigurationRepository.updateCCLConfiguration() } returns false
        getInstance().updateIfRequired()
        verify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdateAfterCertificateChange(false) }
    }

    @Test
    fun `updateIfRequired should NOT update booster rules and ccl configuration if NOT required`() = runBlockingTest {
        coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
        coEvery { timeStamper.nowUTC } returns Instant.parse("2000-01-01T00:00:00Z")

        getInstance().updateIfRequired()

        verify { boosterRulesRepository wasNot Called }
        verify { cclConfigurationRepository wasNot Called }

        verify { dccWalletInfoUpdateTrigger wasNot Called }
    }

    @Test
    fun `updateConfiguration() should return true when new booster rules or new configuration was downloaded or false otherwise`() =
        runBlockingTest {

            val updater = getInstance()

            coEvery { boosterRulesRepository.update() } returns false
            coEvery { cclConfigurationRepository.updateCCLConfiguration() } returns false
            updater.updateConfiguration() shouldBe false

            coEvery { boosterRulesRepository.update() } returns true
            coEvery { cclConfigurationRepository.updateCCLConfiguration() } returns false
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns false
            coEvery { cclConfigurationRepository.updateCCLConfiguration() } returns true
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns true
            coEvery { cclConfigurationRepository.updateCCLConfiguration() } returns true
            updater.updateConfiguration() shouldBe true
        }

    @Test
    fun `isUpdateRequires() should return true after one day`() = runBlockingTest {

        val updater = getInstance()

        // update is required when none was performed yet
        coEvery { cclSettings.getLastExecutionTime() } returns null
        updater.isUpdateRequired(Instant.now()) shouldBe true

        coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")

        // no update required on the same day
        val sameDayBeginning = Instant.parse("2000-01-01T00:00:00Z")
        val sameDayEnd = Instant.parse("2000-01-01T23:59:59Z")
        updater.isUpdateRequired(sameDayBeginning) shouldBe false
        updater.isUpdateRequired(sameDayEnd) shouldBe false

        // update required on next day
        val nextDay = Instant.parse("2000-01-02T00:00:00Z")
        updater.isUpdateRequired(nextDay) shouldBe true

        // update should also happen on previous day (can happen when user fumbles with the device date)
        val previousDay = Instant.parse("1999-12-31T00:00:00Z")
        updater.isUpdateRequired(previousDay)
    }

    private fun getInstance(): CCLConfigurationUpdater {
        return CCLConfigurationUpdater(
            timeStamper,
            cclSettings,
            boosterRulesRepository,
            cclConfigurationRepository,
            dccWalletInfoUpdateTrigger
        )
    }
}
