package de.rki.coronawarnapp.ccl.configuration.update

import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateChangeObserver
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.repositories.UpdateResult
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

@Suppress("MaxLineLength")
internal class CclConfigurationUpdaterTest : BaseTest() {

    @MockK private lateinit var timeStamper: TimeStamper
    @RelaxedMockK private lateinit var cclSettings: CclSettings
    @MockK private lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK private lateinit var cclConfigurationRepository: CclConfigurationRepository
    @RelaxedMockK private lateinit var dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
    @MockK private lateinit var dccValidationRepository: DccValidationRepository
    @MockK private lateinit var dccValidityStateChangeObserver: DccValidityStateChangeObserver

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `updateIfRequired() should update booster rules, invalidation rules and ccl configuration if required`() =
        runTest {
            coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
            coEvery { timeStamper.nowUTC } returns Instant.parse("2000-01-02T00:00:00Z")

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.NO_UPDATE
            coEvery { dccValidityStateChangeObserver.acknowledgeStateOfCertificate() } just Runs

            getInstance().updateIfRequired()

            coVerify(exactly = 1) {
                boosterRulesRepository.update()
                cclConfigurationRepository.updateCclConfiguration()
                dccValidationRepository.updateInvalidationRules()

                cclSettings.setExecutionTimeToNow(any())
                dccWalletInfoUpdateTrigger.triggerAfterConfigChange(true)
            }

            // false should be passed to the trigger when there are no updates
            coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
            getInstance().updateIfRequired()
            coVerify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerAfterConfigChange(false) }
        }

    @Test
    fun `updateIfRequired() should NOT update if NOT required but should trigger DccWalletInfo recalculation`() =
        runTest {
            coEvery { cclSettings.getLastExecutionTime() } returns Instant.parse("2000-01-01T00:00:00Z")
            coEvery { timeStamper.nowUTC } returns Instant.parse("2000-01-01T00:00:00Z")
            coEvery { dccValidityStateChangeObserver.acknowledgeStateOfCertificate() } just Runs

            getInstance().updateIfRequired()

            verify {
                boosterRulesRepository wasNot Called
                cclConfigurationRepository wasNot Called
                dccValidationRepository wasNot Called
            }

            coVerify(exactly = 1) { dccWalletInfoUpdateTrigger.triggerAfterConfigChange(true) }
        }

    @Test
    fun `updateConfiguration() should return true if anything was downloaded or false otherwise`() =
        runTest {

            val updater = getInstance()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.NO_UPDATE
            updater.updateConfiguration() shouldBe false

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.NO_UPDATE
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.NO_UPDATE
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns UpdateResult.NO_UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.NO_UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.UPDATE
            updater.updateConfiguration() shouldBe true

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.UPDATE
            updater.updateConfiguration() shouldBe true

            verify(exactly = 5) { cclSettings.setExecutionTimeToNow(any()) }
        }

    @Test
    fun `updateConfiguration() should not store execution time if any network request fails`() =
        runTest {

            val updater = getInstance()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.FAIL
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.UPDATE
            updater.updateConfiguration()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.FAIL
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.UPDATE
            updater.updateConfiguration()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.UPDATE
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.UPDATE
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.FAIL
            updater.updateConfiguration()

            coEvery { boosterRulesRepository.update() } returns UpdateResult.FAIL
            coEvery { cclConfigurationRepository.updateCclConfiguration() } returns UpdateResult.FAIL
            coEvery { dccValidationRepository.updateInvalidationRules() } returns UpdateResult.FAIL
            updater.updateConfiguration()

            verify { cclSettings wasNot Called }
        }

    @Test
    fun `isUpdateRequires() should return true after one day`() = runTest {

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
            dccWalletInfoUpdateTrigger,
            dccValidationRepository,
            dccValidityStateChangeObserver
        )
    }
}
