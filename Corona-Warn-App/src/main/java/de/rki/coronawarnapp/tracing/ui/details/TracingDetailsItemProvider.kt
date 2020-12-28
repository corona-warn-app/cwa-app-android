package de.rki.coronawarnapp.tracing.ui.details

import dagger.Reusable
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.AdditionalInfoLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorNormalRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsFailedCalculationBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsLowRiskBox
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TracingDetailsItemProvider @Inject constructor(
    tracingStatus: GeneralTracingStatus,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage,
    private val timeStamper: TimeStamper
) {

    val state: Flow<List<DetailsItem>> = combine(
        tracingStatus.generalStatus,
        riskLevelStorage.latestAndLastSuccessful,
        tracingRepository.activeTracingDaysInRetentionPeriod
    ) { status,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod ->

        val (latestCalc, _) = riskLevelResults.tryLatestResultsWithDefaults()

        mutableListOf<DetailsItem>().apply {
            if (latestCalc.riskState == RiskState.LOW_RISK && latestCalc.matchedKeyCount > 0) {
                add(AdditionalInfoLowRiskBox.Item)
            }

            when (latestCalc.riskState) {
                RiskState.INCREASED_RISK -> BehaviorIncreasedRiskBox.Item
                else -> BehaviorNormalRiskBox.Item(
                    tracingStatus = status,
                    riskState = latestCalc.riskState
                )
            }.also { add(it) }

            if (latestCalc.riskState != RiskState.CALCULATION_FAILED) {
                PeriodLoggedBox.Item(
                    activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod.toInt()
                ).also { add(it) }
            }

            when (latestCalc.riskState) {
                RiskState.LOW_RISK -> DetailsLowRiskBox.Item(
                    riskState = latestCalc.riskState,
                    matchedKeyCount = latestCalc.matchedKeyCount
                )
                RiskState.INCREASED_RISK -> DetailsIncreasedRiskBox.Item(
                    riskState = latestCalc.riskState,
                    lastEncounterDaysAgo = Duration(
                        latestCalc.lastRiskEncounterAt ?: Instant.EPOCH,
                        timeStamper.nowUTC
                    ).standardDays.toInt()
                )
                RiskState.CALCULATION_FAILED -> DetailsFailedCalculationBox.Item
            }.also { add(it) }
        }
    }
        .onStart { Timber.v("TracingDetailsState FLOW start") }
        .onEach { Timber.d("TracingDetailsState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingDetailsState FLOW completed.") }
}
