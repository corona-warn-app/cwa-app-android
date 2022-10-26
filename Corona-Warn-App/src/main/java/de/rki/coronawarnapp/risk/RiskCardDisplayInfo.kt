package de.rki.coronawarnapp.risk

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.positiveViewedTests
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import java.time.Duration
import javax.inject.Inject

@Reusable
class RiskCardDisplayInfo @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val testRepository: CoronaTestRepository,
    private val timeStamper: TimeStamper
) {

    /**
     * @param riskState: the current risk state or null if tracing is disabled or the calculation is currently
     * in progress
     * @returns true if the risk card should be displayed, false otherwise
     */
    suspend fun shouldShowRiskCard(riskState: RiskState?): Boolean {

        // High risk card should always be shown regardless of the state of the tests
        if (riskState == RiskState.INCREASED_RISK) {
            return true
        }

        val allPositiveTests = testRepository.positiveViewedTests.first()
        return allPositiveTests.all { it.shouldShowRiskCardForTestType() }
    }

    private suspend fun BaseCoronaTest.shouldShowRiskCardForTestType(): Boolean {
        val coronaTestConfig = appConfigProvider.currentConfig.first().coronaTestParameters

        val (testTimestamp, thresholdDuration) = when (this) {
            is PCRCoronaTest -> registeredAt to coronaTestConfig.pcrParameters.durationToShowRiskCard
            is RACoronaTest -> testTakenAt to coronaTestConfig.ratParameters.durationToShowRiskCard
            else -> error("Unsupported test type=$type")
        }
        return Duration.between(testTimestamp, timeStamper.nowUTC) >= thresholdDuration
    }
}
