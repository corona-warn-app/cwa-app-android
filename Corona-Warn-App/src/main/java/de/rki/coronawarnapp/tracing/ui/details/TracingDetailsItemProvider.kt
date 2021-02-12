package de.rki.coronawarnapp.tracing.ui.details

import dagger.Reusable
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.AdditionalInfoLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorNormalRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsFailedCalculationBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.survey.UserSurveyBox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TracingDetailsItemProvider @Inject constructor(
    tracingStatus: GeneralTracingStatus,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage,
    surveys: Surveys
) {

    val state: Flow<List<DetailsItem>> = combine(
        tracingStatus.generalStatus,
        riskLevelStorage.latestAndLastSuccessful,
        tracingRepository.activeTracingDaysInRetentionPeriod,
        surveys.availableSurveys
    ) { status,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod,
        availableSurveys ->

        val (latestCalc, _) = riskLevelResults.tryLatestResultsWithDefaults()

        mutableListOf<DetailsItem>().apply {
            if (status != Status.TRACING_INACTIVE &&
                latestCalc.riskState == RiskState.LOW_RISK &&
                latestCalc.matchedKeyCount > 0
            ) {
                add(AdditionalInfoLowRiskBox.Item)
            }

            when {
                status != Status.TRACING_INACTIVE && latestCalc.riskState == RiskState.INCREASED_RISK ->
                    BehaviorIncreasedRiskBox.Item
                else -> BehaviorNormalRiskBox.Item(
                    tracingStatus = status,
                    riskState = latestCalc.riskState
                )
            }.also { add(it) }

            if (status != Status.TRACING_INACTIVE &&
                latestCalc.riskState == RiskState.INCREASED_RISK &&
                availableSurveys.contains(Surveys.Type.HIGH_RISK_ENCOUNTER)
            ) {
                add(UserSurveyBox.Item(Surveys.Type.HIGH_RISK_ENCOUNTER))
            }

            if (latestCalc.riskState != RiskState.CALCULATION_FAILED && status != Status.TRACING_INACTIVE) {
                PeriodLoggedBox.Item(
                    activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod.toInt(),
                    tracingStatus = status
                ).also { add(it) }
            }

            when {
                status == Status.TRACING_INACTIVE || latestCalc.riskState == RiskState.CALCULATION_FAILED -> {
                    DetailsFailedCalculationBox.Item
                }
                latestCalc.riskState == RiskState.LOW_RISK -> DetailsLowRiskBox.Item(
                    riskState = latestCalc.riskState,
                    matchedKeyCount = latestCalc.matchedKeyCount
                )
                latestCalc.riskState == RiskState.INCREASED_RISK -> DetailsIncreasedRiskBox.Item(
                    riskState = latestCalc.riskState,
                    lastEncounteredAt = latestCalc.lastRiskEncounterAt ?: Instant.EPOCH
                )
                else -> null
            }?.let { add(it) }
        }
    }
        .onStart { Timber.v("TracingDetailsState FLOW start") }
        .onEach { Timber.d("TracingDetailsState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingDetailsState FLOW completed.") }
}
