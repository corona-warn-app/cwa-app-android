package de.rki.coronawarnapp.worker

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.worker.execution.PCRTestResultScheduler
import de.rki.coronawarnapp.deniability.NoiseScheduler
import de.rki.coronawarnapp.risk.execution.RiskWorkScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class for background work handling
 * The helper uses externalised constants and helper for readability.
 *
 * @see BackgroundConstants
 * @see BackgroundWorkHelper
 */
@Singleton
class BackgroundWorkScheduler @Inject constructor(
    private val riskWorkScheduler: RiskWorkScheduler,
    private val coronaTestRepository: CoronaTestRepository,
    private val testResultScheduler: PCRTestResultScheduler,
    private val noiseScheduler: NoiseScheduler,
) {

    fun startWorkScheduler() {
        Timber.d("startWorkScheduler()")
        riskWorkScheduler.setPeriodicRiskCalculation(enabled = true)

        // TODO Blocking isn't very nice here...
        val coronatests = runBlocking { coronaTestRepository.coronaTests.first() }

        val isSubmissionSuccessful = coronatests.any { it.isSubmitted }
        val hasPendingTests = coronatests.any { !it.isResultAvailableNotificationSent }

        if (!isSubmissionSuccessful && hasPendingTests) {
            testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = true)
        }
    }

    fun stopWorkScheduler() {
        noiseScheduler.setPeriodicNoise(enabled = false)
        riskWorkScheduler.setPeriodicRiskCalculation(enabled = false)
        testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = false)
        Timber.d("All Background Jobs Stopped")
    }
}
