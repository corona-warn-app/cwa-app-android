package de.rki.coronawarnapp.risk

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.Infectiousness
import com.google.android.gms.nearby.exposurenotification.ReportType
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DefaultRiskLevels @Inject constructor() : RiskLevels {

    override fun determineRisk(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindows: List<ExposureWindow>
    ): AggregatedRiskResult {
        val riskResultsPerWindow =
            exposureWindows.mapNotNull { window ->
                calculateRisk(appConfig, window)?.let { window to it }
            }.toMap()

        return aggregateResults(appConfig, riskResultsPerWindow)
    }

    private fun ExposureWindow.dropDueToMinutesAtAttenuation(
        attenuationFilters: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>
    ) =
        attenuationFilters.any { attenuationFilter ->
            // Get total seconds at attenuation in exposure window
            val secondsAtAttenuation: Double = scanInstances
                .filter { attenuationFilter.attenuationRange.inRange(it.minAttenuationDb) }
                .fold(.0) { acc, scanInstance -> acc + max(scanInstance.secondsSinceLastScan, 0) }

            val minutesAtAttenuation = secondsAtAttenuation / 60
            return attenuationFilter.dropIfMinutesInRange.inRange(minutesAtAttenuation)
        }

    private fun ExposureWindow.determineTransmissionRiskLevel(
        transmissionRiskLevelEncoding: RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding
    ): Int {

        val reportTypeOffset = when (reportType) {
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

        val infectiousnessOffset = when (infectiousness) {
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

    private fun ExposureWindow.determineWeightedSeconds(
        minutesAtAttenuationWeight: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>
    ): Double =
        scanInstances.fold(.0) { seconds, scanInstance ->
            val weight: Double =
                minutesAtAttenuationWeight
                    .filter { it.attenuationRange.inRange(scanInstance.minAttenuationDb) }
                    .map { it.weight }
                    .firstOrNull() ?: .0
            seconds + max(scanInstance.secondsSinceLastScan, 0) * weight
        }

    private fun determineRiskLevel(
        normalizedTime: Double,
        timeToRiskLevelMapping: List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>
    ): ProtoRiskLevel? =
        timeToRiskLevelMapping
            .filter { it.normalizedTimeRange.inRange(normalizedTime) }
            .map { it.riskLevel }
            .firstOrNull()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun calculateRisk(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindow: ExposureWindow
    ): RiskResult? {
        if (exposureWindow.dropDueToMinutesAtAttenuation(appConfig.minutesAtAttenuationFilters)) {
            Timber.d("%s dropped due to minutes at attenuation filter", exposureWindow)
            return null
        }

        val transmissionRiskLevel: Int = exposureWindow.determineTransmissionRiskLevel(
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

        val transmissionRiskValue: Double =
            transmissionRiskLevel * appConfig.transmissionRiskLevelMultiplier

        Timber.d("%s's transmissionRiskValue is: %s", exposureWindow, transmissionRiskValue)

        val weightedMinutes: Double = exposureWindow.determineWeightedSeconds(
            appConfig.minutesAtAttenuationWeights
        ) / 60f

        Timber.d("%s's weightedMinutes are: %s", exposureWindow, weightedMinutes)

        val normalizedTime: Double = transmissionRiskValue * weightedMinutes

        Timber.d("%s's normalizedTime is: %s", exposureWindow, normalizedTime)

        val riskLevel: ProtoRiskLevel? = determineRiskLevel(
            normalizedTime,
            appConfig.normalizedTimePerExposureWindowToRiskLevelMapping
        )

        if (riskLevel == null) {
            Timber.e("Exposure Window: $exposureWindow could not be mapped to a risk level")
            throw NormalizedTimePerExposureWindowToRiskLevelMappingMissingException()
        }

        Timber.d("%s's riskLevel is: %s", exposureWindow, riskLevel)

        return RiskResult(
            transmissionRiskLevel = transmissionRiskLevel,
            normalizedTime = normalizedTime,
            riskLevel = riskLevel
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun aggregateResults(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskResult {
        val uniqueDatesMillisSinceEpoch = exposureWindowsAndResult.keys
            .map { it.dateMillisSinceEpoch }
            .toSet()

        Timber.d(
            "uniqueDates: %s", { TextUtils.join(System.lineSeparator(), uniqueDatesMillisSinceEpoch) }
        )
        val exposureHistory = uniqueDatesMillisSinceEpoch.map {
            aggregateRiskPerDate(appConfig, it, exposureWindowsAndResult)
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
        val mostRecentDateWithLowRisk =
            exposureHistory.mostRecentDateForRisk(ProtoRiskLevel.LOW)

        Timber.d("mostRecentDateWithLowRisk: $mostRecentDateWithLowRisk")

        // 8. Determine `Date of Most Recent Date with High Risk`
        val mostRecentDateWithHighRisk =
            exposureHistory.mostRecentDateForRisk(ProtoRiskLevel.HIGH)

        Timber.d("mostRecentDateWithHighRisk: $mostRecentDateWithHighRisk")

        // 9. Determine `Total Minimum Distinct Encounters With Low Risk`
        val totalMinimumDistinctEncountersWithLowRisk = exposureHistory
            .sumBy { it.minimumDistinctEncountersWithLowRisk }

        Timber.d("totalMinimumDistinctEncountersWithLowRisk: $totalMinimumDistinctEncountersWithLowRisk")

        // 10. Determine `Total Minimum Distinct Encounters With High Risk`
        val totalMinimumDistinctEncountersWithHighRisk = exposureHistory
            .sumBy { it.minimumDistinctEncountersWithHighRisk }

        Timber.d("totalMinimumDistinctEncountersWithHighRisk: $totalMinimumDistinctEncountersWithHighRisk")

        // 11. Determine `Number of Days With Low Risk`
        val numberOfDaysWithLowRisk =
            exposureHistory.numberOfDaysForRisk(ProtoRiskLevel.LOW)

        Timber.d("numberOfDaysWithLowRisk: $numberOfDaysWithLowRisk")

        // 12. Determine `Number of Days With High Risk`
        val numberOfDaysWithHighRisk =
            exposureHistory.numberOfDaysForRisk(ProtoRiskLevel.HIGH)

        Timber.d("numberOfDaysWithHighRisk: $numberOfDaysWithHighRisk")

        return AggregatedRiskResult(
            totalRiskLevel = totalRiskLevel,
            totalMinimumDistinctEncountersWithLowRisk = totalMinimumDistinctEncountersWithLowRisk,
            totalMinimumDistinctEncountersWithHighRisk = totalMinimumDistinctEncountersWithHighRisk,
            mostRecentDateWithLowRisk = mostRecentDateWithLowRisk,
            mostRecentDateWithHighRisk = mostRecentDateWithHighRisk,
            numberOfDaysWithLowRisk = numberOfDaysWithLowRisk,
            numberOfDaysWithHighRisk = numberOfDaysWithHighRisk
        )
    }

    private fun List<AggregatedRiskPerDateResult>.mostRecentDateForRisk(riskLevel: ProtoRiskLevel): Instant? =
        filter { it.riskLevel == riskLevel }
            .maxOfOrNull { it.dateMillisSinceEpoch }
            ?.let { Instant.ofEpochMilli(it) }

    private fun List<AggregatedRiskPerDateResult>.numberOfDaysForRisk(riskLevel: ProtoRiskLevel): Int =
        filter { it.riskLevel == riskLevel }
            .size

    private fun aggregateRiskPerDate(
        appConfig: ExposureWindowRiskCalculationConfig,
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
            throw NormalizedTimePerDayToRiskLevelMappingMissingException()
        }

        Timber.d("riskLevel: ${riskLevel.name} (${riskLevel.ordinal})")

        // 4. Determine `Minimum Distinct Encounters With Low Risk per Date`
        val minimumDistinctEncountersWithLowRisk =
            exposureWindowsAndResultForDate.minimumDistinctEncountersForRisk(ProtoRiskLevel.LOW)

        Timber.d("minimumDistinctEncountersWithLowRisk: $minimumDistinctEncountersWithLowRisk")

        // 5. Determine `Minimum Distinct Encounters With High Risk per Date`
        val minimumDistinctEncountersWithHighRisk =
            exposureWindowsAndResultForDate.minimumDistinctEncountersForRisk(ProtoRiskLevel.HIGH)

        Timber.d("minimumDistinctEncountersWithHighRisk: $minimumDistinctEncountersWithHighRisk")

        return AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = dateMillisSinceEpoch,
            riskLevel = riskLevel,
            minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersWithHighRisk
        )
    }

    private fun Map<ExposureWindow, RiskResult>.minimumDistinctEncountersForRisk(riskLevel: ProtoRiskLevel): Int =
        filter { it.value.riskLevel == riskLevel }
            .map { "${it.value.transmissionRiskLevel}_${it.key.calibrationConfidence}" }
            .distinct()
            .size

    companion object {

        open class RiskLevelMappingMissingException(msg: String) : Exception(msg)

        class NormalizedTimePerExposureWindowToRiskLevelMappingMissingException : RiskLevelMappingMissingException(
            "Failed to map the normalized Time per Exposure Window to a Risk Level"
        )

        class NormalizedTimePerDayToRiskLevelMappingMissingException : RiskLevelMappingMissingException(
            "Failed to map the normalized Time per Day to a Risk Level"
        )

        class UnknownReportTypeException : Exception(
            "The Report Type returned by the ENF is not known"
        )

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
