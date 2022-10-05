package de.rki.coronawarnapp.tracing.ui.details

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.installTime.InstallTimeProvider
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.AdditionalInfoLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.FindDetailsInJournalBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorNormalRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsFailedCalculationBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.survey.UserSurveyBox
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@Reusable
class TracingDetailsItemProvider @Inject constructor(
    tracingStatus: GeneralTracingStatus,
    riskLevelStorage: RiskLevelStorage,
    installTimeProvider: InstallTimeProvider,
    appConfigProvider: AppConfigProvider,
    surveys: Surveys
) {

    val state: Flow<List<DetailsItem>> = combine(
        tracingStatus.generalStatus,
        riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult,
        surveys.availableSurveys,
        appConfigProvider.currentConfig,
    ) { status,
        riskLevelResults,
        availableSurveys,
        appConfig ->

        val latestCalc = riskLevelResults.lastCalculated

        val lowRiskWithEncounters = latestCalc.riskState == RiskState.LOW_RISK &&
            latestCalc.daysWithEncounters > 0

        mutableListOf<DetailsItem>().apply {
            if (status != Status.TRACING_INACTIVE &&
                (lowRiskWithEncounters || latestCalc.riskState == RiskState.INCREASED_RISK)
            ) {
                add(FindDetailsInJournalBox.Item(latestCalc.riskState))
            }

            if (status != Status.TRACING_INACTIVE && lowRiskWithEncounters) {
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
                    daysSinceInstallation = installTimeProvider.daysSinceInstallation,
                    tracingStatus = status,
                    maxEncounterAgeInDays = appConfig.maxEncounterAgeInDays
                ).also { add(it) }
            }

            when {
                status == Status.TRACING_INACTIVE || latestCalc.riskState == RiskState.CALCULATION_FAILED -> {
                    DetailsFailedCalculationBox.Item
                }
                latestCalc.riskState == RiskState.LOW_RISK -> DetailsLowRiskBox.Item(
                    riskState = latestCalc.riskState,
                    matchedRiskCount = latestCalc.daysWithEncounters
                )
                latestCalc.riskState == RiskState.INCREASED_RISK -> DetailsIncreasedRiskBox.Item(
                    riskState = latestCalc.riskState,
                    lastEncounteredAt = latestCalc.lastRiskEncounterAt ?: Instant.EPOCH.toLocalDateUtc()
                )
                else -> null
            }?.let { add(it) }
        }
    }
        .onStart { Timber.v("TracingDetailsState FLOW start") }
        .onEach { Timber.d("TracingDetailsState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingDetailsState FLOW completed.") }
}
