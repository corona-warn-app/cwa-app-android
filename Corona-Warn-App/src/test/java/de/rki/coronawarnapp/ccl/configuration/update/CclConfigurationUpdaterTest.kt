package de.rki.coronawarnapp.ccl.configuration.update

import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.repositories.UpdateResult
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
internal class CclConfigurationUpdaterTest : BaseTest() {

    @MockK private lateinit var timeStamper: TimeStamper
    @RelaxedMockK private lateinit var cclSettings: CclSettings
    @MockK private lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK private lateinit var cclConfigurationRepository: CclConfigurationRepository
    @RelaxedMockK private lateinit var dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `updateIfRequired() should update booster rules and ccl configuration if required`() = runBlockingTest {
        coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
        coEvery { cclSettings.forceCclCalculation() } returns false
        coEvery { timeStamper.nowUTC } returns Instant.parse("2000-01-02T00:00:00Z")

        coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
        coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE

        getInstance().updateIfRequired()

        coVerify(exactly = 1) { boosterRulesRepository.update() }
        coVerify(exactly = 1) { cclConfigurationRepository.updateCclConfiguration() }

        verify(exactly = 1) { cclSettings.setExecutionTimeToNow(any()) }

        coVerify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerAfterConfigChange(true) }

        // false should be passed to the trigger when there are no updates
        coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
        coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
        getInstance().updateIfRequired()
        coVerify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerAfterConfigChange(false) }
    }

    @Test
    fun `updateIfRequired() should NOT update booster rules and ccl configuration if NOT required but should trigger DccWalletInfo recalculation`() =
        runBlockingTest {
            coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
            coEvery { timeStamper.nowUTC } returns Instant.parse("2000-01-01T00:00:00Z")

            getInstance().updateIfRequired()

            verify { boosterRulesRepository wasNot Called }
            verify { cclConfigurationRepository wasNot Called }

            coVerify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerAfterConfigChange(false) }
        }

    @Test
    fun `updateConfiguration() should return true when new booster rules or new configuration was downloaded or false otherwise`() =
        runBlockingTest {

            val updater = getInstance()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
            updater.updateConfiguration() shouldBe false

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            updater.updateConfiguration() shouldBe true

            verify(exactly = 4) { cclSettings.setExecutionTimeToNow(any()) }
        }

    @Test
    fun `updateConfiguration() should not store execution time when at least one network request fails`() =
        runBlockingTest {

            val updater = getInstance()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.FAIL
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            updater.updateConfiguration()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.FAIL
            updater.updateConfiguration()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.FAIL
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.FAIL
            updater.updateConfiguration()

            verify { cclSettings wasNot Called }
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

    private fun getInstance(): CclConfigurationUpdater {
        return CclConfigurationUpdater(
            timeStamper,
            cclSettings,
            boosterRulesRepository,
            cclConfigurationRepository,
            dccWalletInfoUpdateTrigger
        )
    }
}
