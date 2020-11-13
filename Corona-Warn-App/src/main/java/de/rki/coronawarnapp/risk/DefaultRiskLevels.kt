package de.rki.coronawarnapp.risk

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.Infectiousness
import com.google.android.gms.nearby.exposurenotification.ReportType
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.download.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.exception.RiskLevelCalculationException
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHours
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRiskLevels @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) : RiskLevels {

    private var appConfig: ConfigData

    init {
        runBlocking {
            appConfig = appConfigProvider.getAppConfig()
        }

        appConfigProvider.currentConfig
            .onEach { if (it != null) appConfig = it }
    }

    override fun updateRepository(riskLevel: RiskLevel, time: Long) {
        val rollbackItems = mutableListOf<RollbackItem>()
        try {
            Timber.tag(TAG).v("Update the risk level with $riskLevel")
            val lastCalculatedRiskLevelScoreForRollback =
                RiskLevelRepository.getLastCalculatedScore()
            updateRiskLevelScore(riskLevel)
            rollbackItems.add {
                updateRiskLevelScore(lastCalculatedRiskLevelScoreForRollback)
            }

            // risk level calculation date update
            val lastCalculatedRiskLevelDate = LocalData.lastTimeRiskLevelCalculation()
            LocalData.lastTimeRiskLevelCalculation(time)
            rollbackItems.add {
                LocalData.lastTimeRiskLevelCalculation(lastCalculatedRiskLevelDate)
            }
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Updating the RiskLevelRepository failed.")

            try {
                Timber.tag(TAG).d("Initiate Rollback")
                for (rollbackItem: RollbackItem in rollbackItems) rollbackItem.invoke()
            } catch (rollbackException: Exception) {
                Timber.tag(TAG).e(rollbackException, "RiskLevelRepository rollback failed.")
            }

            throw error
        }
    }

    override fun calculationNotPossibleBecauseOfOutdatedResults(): Boolean {
        // if the last calculation is longer in the past as the defined threshold we return the stale state
        val timeSinceLastDiagnosisKeyFetchFromServer =
            TimeVariables.getTimeSinceLastDiagnosisKeyFetchFromServer()
                ?: throw RiskLevelCalculationException(
                    IllegalArgumentException(
                        "Time since last exposure calculation is null"
                    )
                )
        /** we only return outdated risk level if the threshold is reached AND the active tracing time is above the
        defined threshold because [UNKNOWN_RISK_INITIAL] overrules [UNKNOWN_RISK_OUTDATED_RESULTS] */
        return timeSinceLastDiagnosisKeyFetchFromServer.millisecondsToHours() >
            TimeVariables.getMaxStaleExposureRiskRange() && isActiveTracingTimeAboveThreshold()
    }

    override fun calculationNotPossibleBecauseOfNoKeys() =
        (TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() == null).also {
            if (it) {
                Timber.tag(TAG)
                    .v("No last time diagnosis keys from server fetch timestamp was found")
            }
        }

    override fun isIncreasedRisk(exposureWindows: List<ExposureWindow>): Boolean {
        val riskResultsPerWindow =
            exposureWindows.mapNotNull {
                val risk = calculateRisk(it) ?: return@mapNotNull null
                it to risk
            }.toMap()

        val aggregatedResult = aggregateResults(riskResultsPerWindow)

        return aggregatedResult.totalRiskLevel ==
            RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH
    }

    override fun isActiveTracingTimeAboveThreshold(): Boolean {
        val durationTracingIsActive = TimeVariables.getTimeActiveTracingDuration()
        val activeTracingDurationInHours = durationTracingIsActive.millisecondsToHours()
        val durationTracingIsActiveThreshold =
            TimeVariables.getMinActivatedTracingTime().toLong()

        return (activeTracingDurationInHours >= durationTracingIsActiveThreshold).also {
            Timber.tag(TAG).v(
                "Active tracing time ($activeTracingDurationInHours h) is above threshold " +
                    "($durationTracingIsActiveThreshold h): $it"
            )
            if (it) {
                Timber.tag(TAG).v("Active tracing time is not enough")
            }
        }
    }

    @VisibleForTesting
    internal fun withinDefinedLevelThreshold(riskScore: Double, min: Int, max: Int) =
        riskScore >= min && riskScore <= max

    /**
     * Updates the Risk Level Score in the repository with the calculated Risk Level
     *
     * @param riskLevel
     */
    @VisibleForTesting
    internal fun updateRiskLevelScore(riskLevel: RiskLevel) {
        val lastCalculatedScore = RiskLevelRepository.getLastCalculatedScore()
        Timber.d("last CalculatedS core is ${lastCalculatedScore.raw} and Current Risk Level is ${riskLevel.raw}")

        if (RiskLevel.riskLevelChangedBetweenLowAndHigh(
                lastCalculatedScore,
                riskLevel
            ) && !LocalData.submissionWasSuccessful()
        ) {
            Timber.d(
                "Notification Permission = ${
                    NotificationManagerCompat.from(CoronaWarnApplication.getAppContext()).areNotificationsEnabled()
                }"
            )

            NotificationHelper.sendNotification(
                CoronaWarnApplication.getAppContext().getString(R.string.notification_body)
            )

            Timber.d("Risk level changed and notification sent. Current Risk level is ${riskLevel.raw}")
        }
        if (lastCalculatedScore.raw == RiskLevelConstants.INCREASED_RISK &&
            riskLevel.raw == RiskLevelConstants.LOW_LEVEL_RISK
        ) {
            LocalData.isUserToBeNotifiedOfLoweredRiskLevel = true

            Timber.d("Risk level changed LocalData is updated. Current Risk level is ${riskLevel.raw}")
        }
        RiskLevelRepository.setRiskLevelScore(riskLevel)
    }

    private fun dropDueToMinutesAtAttenuation(
        exposureWindow: ExposureWindow,
        attenuationFilters: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>
    ) =
        attenuationFilters.any { attenuationFilter ->
            // Get total seconds at attenuation in exposure window
            val secondsAtAttenuation = exposureWindow.scanInstances
                .filter { attenuationFilter.attenuationRange.inRange(it.typicalAttenuationDb) }
                .fold(0) { acc, scanInstance -> acc + scanInstance.secondsSinceLastScan }

            val minutesAtAttenuation = secondsAtAttenuation / 60
            return attenuationFilter.dropIfMinutesInRange.inRange(minutesAtAttenuation)
        }

    private fun determineTransmissionRiskLevel(
        exposureWindow: ExposureWindow,
        transmissionRiskLevelEncoding: RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding
    ): Int {

        val reportTypeOffset = when (exposureWindow.reportType) {
            ReportType.RECURSIVE -> transmissionRiskLevelEncoding
                .reportTypeOffsetRecursive
            ReportType.SELF_REPORT -> transmissionRiskLevelEncoding
                .reportTypeOffsetSelfReport
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS -> transmissionRiskLevelEncoding
                .reportTypeOffsetConfirmedClinicalDiagnosis
            ReportType.CONFIRMED_TEST -> transmissionRiskLevelEncoding
                .reportTypeOffsetConfirmedTest
            else -> throw UnknownReportTypeException()
        }

        val infectiousnessOffset = when (exposureWindow.infectiousness) {
            Infectiousness.HIGH -> transmissionRiskLevelEncoding
                .infectiousnessOffsetHigh
            else -> transmissionRiskLevelEncoding
                .infectiousnessOffsetStandard
        }

        return reportTypeOffset + infectiousnessOffset
    }

    private fun dropDueToTransmissionRiskLevel(
        transmissionRiskLevel: Int,
        transmissionRiskLevelFilters: List<RiskCalculationParametersOuterClass.TrlFilter>
    ) =
        transmissionRiskLevelFilters.any {
            it.dropIfTrlInRange.inRange(transmissionRiskLevel)
        }

    private fun determineWeightedSeconds(
        exposureWindow: ExposureWindow,
        minutesAtAttenuationWeight: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>
    ): Double =
        exposureWindow.scanInstances.fold(.0) { seconds, scanInstance ->
            val weight =
                minutesAtAttenuationWeight
                    .filter { it.attenuationRange.inRange(scanInstance.typicalAttenuationDb) }
                    .map { it.weight }
                    .firstOrNull() ?: .0
            seconds + scanInstance.secondsSinceLastScan * weight
        }

    private fun determineRiskLevel(
        normalizedTime: Double,
        timeToRiskLevelMapping: List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>
    ) =
        timeToRiskLevelMapping
            .filter { it.normalizedTimeRange.inRange(normalizedTime) }
            .map { it.riskLevel }
            .firstOrNull()

    override fun calculateRisk(
        exposureWindow: ExposureWindow
    ): RiskResult? {
        if (dropDueToMinutesAtAttenuation(exposureWindow, appConfig.minutesAtAttenuationFilters)) {
            Timber.d(
                "%s dropped due to minutes at attenuation filter",
                exposureWindow
            )
            return null
        }

        val transmissionRiskLevel = determineTransmissionRiskLevel(
            exposureWindow,
            appConfig.transmissionRiskLevelEncoding
        )

        if (dropDueToTransmissionRiskLevel(transmissionRiskLevel, appConfig.transmissionRiskLevelFilters)) {
            Timber.d(
                "%s dropped due to transmission risk level filter, level is %s",
                exposureWindow,
                transmissionRiskLevel
            )
            return null
        }

        val transmissionRiskValue =
            transmissionRiskLevel * appConfig.transmissionRiskLevelMultiplier

        Timber.d(
            "%s's transmissionRiskValue is: %s",
            exposureWindow,
            transmissionRiskValue
        )

        val weightedMinutes = determineWeightedSeconds(
            exposureWindow,
            appConfig.minutesAtAttenuationWeights
        ) / 60

        Timber.d(
            "%s's weightedMinutes are: %s",
            exposureWindow,
            weightedMinutes
        )

        val normalizedTime = transmissionRiskValue * weightedMinutes

        Timber.d(
            "%s's normalizedTime is: %s",
            exposureWindow,
            normalizedTime
        )

        val riskLevel = determineRiskLevel(
            normalizedTime,
            appConfig.normalizedTimePerExposureWindowToRiskLevelMapping
        )

        if (riskLevel == null) {
            Timber.e("Exposure Window: $exposureWindow could not be mapped to a risk level")
            throw NormalizedTimePerExposureWindowToRiskLevelMappingMissingException()
        }

        Timber.d(
            "%s's riskLevel is: %s",
            exposureWindow,
            riskLevel
        )

        return RiskResult(transmissionRiskLevel, normalizedTime, riskLevel)
    }

    override fun aggregateResults(
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskResult {
        val uniqueDatesMillisSinceEpoch = exposureWindowsAndResult.keys
            .map { it.dateMillisSinceEpoch }
            .toSet()

        Timber.d(
            "uniqueDates: ${
                TextUtils.join(System.lineSeparator(), uniqueDatesMillisSinceEpoch)
            }"
        )
        val exposureHistory = uniqueDatesMillisSinceEpoch.map {
            aggregateRiskPerDate(it, exposureWindowsAndResult)
        }

        Timber.d("exposureHistory size: ${exposureHistory.size}")

        // 6. Determine `Total Risk`
        val totalRiskLevel =
            if (exposureHistory.any {
                    it.riskLevel == RiskCalculationParametersOuterClass
                        .NormalizedTimeToRiskLevelMapping
                        .RiskLevel
                        .HIGH
                }) {
                RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH
            } else {
                RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
            }

        Timber.d("totalRiskLevel: ${totalRiskLevel.name} (${totalRiskLevel.ordinal})")

        // 7. Determine `Date of Most Recent Date with Low Risk`
        val mostRecentDateWithLowRisk = mostRecentDateForRisk(
            exposureHistory,
            RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
        )

        Timber.d("mostRecentDateWithLowRisk: $mostRecentDateWithLowRisk")

        // 8. Determine `Date of Most Recent Date with High Risk`
        val mostRecentDateWithHighRisk = mostRecentDateForRisk(
            exposureHistory,
            RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH
        )

        Timber.d("mostRecentDateWithHighRisk: $mostRecentDateWithHighRisk")

        // 9. Determine `Total Minimum Distinct Encounters With Low Risk`
        val totalMinimumDistinctEncountersWithLowRisk = exposureHistory
            .sumBy { it.minimumDistinctEncountersWithLowRisk }

        Timber.d("totalMinimumDistinctEncountersWithLowRisk: $totalMinimumDistinctEncountersWithLowRisk")

        // 10. Determine `Total Minimum Distinct Encounters With High Risk`
        val totalMinimumDistinctEncountersWithHighRisk = exposureHistory
            .sumBy { it.minimumDistinctEncountersWithHighRisk }

        Timber.d("totalMinimumDistinctEncountersWithHighRisk: $totalMinimumDistinctEncountersWithHighRisk")

        return AggregatedRiskResult(
            totalRiskLevel,
            totalMinimumDistinctEncountersWithLowRisk,
            totalMinimumDistinctEncountersWithHighRisk,
            mostRecentDateWithLowRisk,
            mostRecentDateWithHighRisk
        )
    }

    private fun mostRecentDateForRisk(
        exposureHistory: List<AggregatedRiskPerDateResult>,
        riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
    ): Instant? = exposureHistory
        .filter { it.riskLevel == riskLevel }
        .maxOfOrNull { it.dateMillisSinceEpoch }
        ?.let { Instant.ofEpochMilli(it) }

    private fun aggregateRiskPerDate(
        dateMillisSinceEpoch: Long,
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskPerDateResult {
        // 1. Group `Exposure Windows by Date`
        val exposureWindowsAndResultForDate = exposureWindowsAndResult
            .filter { it.key.dateMillisSinceEpoch == dateMillisSinceEpoch }

        // 2. Determine `Normalized Time per Date`
        val normalizedTime = exposureWindowsAndResultForDate.values
            .sumOf { it.normalizedTime }

        Timber.d("Aggregating result for date $dateMillisSinceEpoch - ${Instant.ofEpochMilli(dateMillisSinceEpoch)}")

        // 3. Determine `Risk Level per Date`
        val riskLevel = try {
            appConfig.normalizedTimePerDayToRiskLevelMappingList
                .filter { it.normalizedTimeRange.inRange(normalizedTime) }
                .map { it.riskLevel }
                .first()
        } catch (e: Exception) {
            throw ApplicationConfigurationInvalidException(
                e,
                "Invalid config for normalizedTimePerDayToRiskLevelMapping"
            )
        }

        Timber.d("riskLevel: ${riskLevel.name} (${riskLevel.ordinal})")

        // 4. Determine `Minimum Distinct Encounters With Low Risk per Date`
        val minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersForRisk(
            exposureWindowsAndResultForDate,
            RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
        )

        Timber.d("minimumDistinctEncountersWithLowRisk: $minimumDistinctEncountersWithLowRisk")

        // 5. Determine `Minimum Distinct Encounters With High Risk per Date`
        val minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersForRisk(
            exposureWindowsAndResultForDate,
            RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH
        )

        Timber.d("minimumDistinctEncountersWithHighRisk: $minimumDistinctEncountersWithHighRisk")

        return AggregatedRiskPerDateResult(
            dateMillisSinceEpoch,
            riskLevel,
            minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk
        )
    }

    private fun minimumDistinctEncountersForRisk(
        exposureWindowsAndResultForDate: Map<ExposureWindow, RiskResult>,
        riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
    ): Int =
        exposureWindowsAndResultForDate
            .filter { it.value.riskLevel == riskLevel }
            .map { "${it.value.transmissionRiskLevel}_${it.key.calibrationConfidence}" }
            .distinct()
            .size

    companion object {
        private val TAG = DefaultRiskLevels::class.java.simpleName
        private const val DECIMAL_MULTIPLIER = 100

        class NormalizedTimePerExposureWindowToRiskLevelMappingMissingException : Exception()
        class UnknownReportTypeException : Exception()

        private fun <T : Number> RiskCalculationParametersOuterClass.Range.inRange(value: T): Boolean =
            when {
                minExclusive && value.toDouble() <= min -> false
                !minExclusive && value.toDouble() < min -> false
                maxExclusive && value.toDouble() >= max -> false
                !maxExclusive && value.toDouble() > max -> false
                else -> true
            }
    }
}
