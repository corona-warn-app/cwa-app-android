package de.rki.coronawarnapp.risk

import android.text.TextUtils
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.Infectiousness
import com.google.android.gms.nearby.exposurenotification.ReportType
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskLevelConfig
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureData
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

@Singleton
class DefaultRiskLevelCalculation @Inject constructor(
    private val exposureWindowRiskLevelConfig: ExposureWindowRiskLevelConfig
) : RiskLevelCalculation {

    companion object {

        private var TAG = DefaultRiskLevelCalculation::class.simpleName

        private const val DECIMAL_MULTIPLIER = 100
    }

    override fun calculateRiskScore(
        attenuationParameters: AttenuationDurationOuterClass.AttenuationDuration,
        exposureSummary: ExposureSummary
    ): Double {

        /** all attenuation values are capped to [TimeVariables.MAX_ATTENUATION_DURATION] */
        val weightedAttenuationLow =
            attenuationParameters.weights.low
                .times(exposureSummary.attenuationDurationsInMinutes[0].capped())
        val weightedAttenuationMid =
            attenuationParameters.weights.mid
                .times(exposureSummary.attenuationDurationsInMinutes[1].capped())
        val weightedAttenuationHigh =
            attenuationParameters.weights.high
                .times(exposureSummary.attenuationDurationsInMinutes[2].capped())

        val maximumRiskScore = exposureSummary.maximumRiskScore.toDouble()

        val defaultBucketOffset = attenuationParameters.defaultBucketOffset.toDouble()
        val normalizationDivisor = attenuationParameters.riskScoreNormalizationDivisor.toDouble()

        val attenuationStrings =
            "Weighted Attenuation: ($weightedAttenuationLow + $weightedAttenuationMid + " +
                "$weightedAttenuationHigh + $defaultBucketOffset)"
        Timber.v(attenuationStrings)

        val weightedAttenuationDuration =
            weightedAttenuationLow
                .plus(weightedAttenuationMid)
                .plus(weightedAttenuationHigh)
                .plus(defaultBucketOffset)

        Timber.v("Formula used: ($maximumRiskScore / $normalizationDivisor) * $weightedAttenuationDuration")

        val riskScore = (maximumRiskScore / normalizationDivisor) * weightedAttenuationDuration

        return round(riskScore.times(DECIMAL_MULTIPLIER)).div(DECIMAL_MULTIPLIER)
    }

    private fun Int.capped(): Int {
        return if (this > TimeVariables.getMaxAttenuationDuration()) {
            TimeVariables.getMaxAttenuationDuration()
        } else {
            this
        }
    }

    private fun dropDueToMinutesAtAttenuation(exposureWindow: ExposureWindow) =
        exposureWindowRiskLevelConfig.minutesAtAttenuationFilters.any { attenuationFilter ->
            // Get total seconds at attenuation in exposure window
            val secondsAtAttenuation = exposureWindow.scanInstances
                .filter { attenuationFilter.attenuationRange.inRange(it.typicalAttenuationDb) }
                .fold(0) { acc, scanInstance -> acc + scanInstance.secondsSinceLastScan }

            val minutesAtAttenuation = secondsAtAttenuation / 60
            return attenuationFilter.dropIfMinutesInRange.inRange(minutesAtAttenuation)
        }

    private fun determineTransmissionRiskLevel(exposureWindow: ExposureWindow): Int {
        val reportTypeOffset = when (exposureWindow.reportType) {
            ReportType.RECURSIVE -> exposureWindowRiskLevelConfig.transmissionRiskLevelEncoding.reportTypeOffsetRecursive
            ReportType.SELF_REPORT -> exposureWindowRiskLevelConfig.transmissionRiskLevelEncoding.reportTypeOffsetSelfReport
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS -> exposureWindowRiskLevelConfig.transmissionRiskLevelEncoding.reportTypeOffsetConfirmedClinicalDiagnosis
            ReportType.CONFIRMED_TEST -> exposureWindowRiskLevelConfig.transmissionRiskLevelEncoding.reportTypeOffsetConfirmedTest
            else -> throw UnknownReportTypeException()
        }

        val infectiousnessOffset = when (exposureWindow.infectiousness) {
            Infectiousness.HIGH -> exposureWindowRiskLevelConfig.transmissionRiskLevelEncoding.infectiousnessOffsetHigh
            else -> exposureWindowRiskLevelConfig.transmissionRiskLevelEncoding.infectiousnessOffsetStandard
        }

        return reportTypeOffset + infectiousnessOffset
    }

    private fun dropDueToTransmissionRiskLevel(transmissionRiskLevel: Int) =
        exposureWindowRiskLevelConfig.transmissionRiskLevelFilters.any {
            it.dropIfTrlInRange.inRange(transmissionRiskLevel)
        }

    private fun determineWeightedSeconds(exposureWindow: ExposureWindow): Double =
        exposureWindow.scanInstances.fold(.0) { seconds, scanInstance ->
            val weight =
                exposureWindowRiskLevelConfig.minutesAtAttenuationWeights
                    .filter { it.attenuationRange.inRange(scanInstance.typicalAttenuationDb) }
                    .map { it.weight }
                    .firstOrNull() ?: .0
            return seconds + scanInstance.secondsSinceLastScan * weight
        }

    private fun determineRiskLevel(normalizedTime: Double) =
        exposureWindowRiskLevelConfig.normalizedTimePerExposureWindowToRiskLevelMapping
            .filter { it.normalizedTimeRange.inRange(normalizedTime) }
            .map { it.riskLevel }
            .firstOrNull()

    override fun calculateRisk(
        exposureWindow: ExposureWindow
    ): RiskResult? {
        if (dropDueToMinutesAtAttenuation(exposureWindow)) {
            return null
        }

        val transmissionRiskLevel = determineTransmissionRiskLevel(exposureWindow)

        if (dropDueToTransmissionRiskLevel(transmissionRiskLevel)) {
            return null
        }

        val transmissionRiskValue =
            transmissionRiskLevel * exposureWindowRiskLevelConfig.transmissionRiskLevelMultiplier

        val weightedMinutes = determineWeightedSeconds(exposureWindow) / 60

        val normalizedTime = transmissionRiskValue * weightedMinutes

        val riskLevel = determineRiskLevel(normalizedTime)
            ?: throw NormalizedTimePerExposureWindowToRiskLevelMappingMissingException()

        return RiskResult(transmissionRiskLevel, normalizedTime, riskLevel)
    }

    override fun aggregateResults(
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>,
        riskCalculationParameters: RiskCalculationParametersOuterClass.RiskCalculationParameters
    ): AggregatedRiskResult {
        val uniqueDates = exposureWindowsAndResult.keys
            .map { it.dateMillisSinceEpoch }
            .toSet()
        Timber.d("uniqueDates: ${TextUtils.join(System.lineSeparator(), uniqueDates)}")

        val exposureHistory = uniqueDates.map {
            exposureDataMapper(
                it,
                exposureWindowsAndResult,
                riskCalculationParameters
            )
        }

        exposureHistory.forEach { Timber.d("(date=${it.date}, riskLevel=${it.riskLevel}, minimumDistinctEncountersWithLowRisk=${it.minimumDistinctEncountersWithLowRisk}, minimumDistinctEncountersWithHighRisk=${it.minimumDistinctEncountersWithHighRisk})") }

        // 6. Determine `Total Risk`
        val totalRiskLevel =
            if (exposureHistory.any { it.riskLevel == RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH }) {
                RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH
            } else {
                RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
            }

        Timber.d("totalRiskLevel: ${totalRiskLevel.name} (${totalRiskLevel.ordinal})")

        // 7. Determine `Date of Most Recent Date with Low Risk`

        // 8. Determine `Date of Most Recent Date with High Risk`

        // 9. Determine `Total Minimum Distinct Encounters With Low Risk`

        // 10. Determine `Total Minimum Distinct Encounters With High Risk`

        TODO("Implement last steps and adjust values")

        return AggregatedRiskResult(
            totalRiskLevel,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 1,
            mostRecentDateWithLowRisk = 1,
            mostRecentDateWithHighRisk = 1
        )
    }

    private fun exposureDataMapper(
        date: Long,
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>,
        riskCalculationParameters: RiskCalculationParametersOuterClass.RiskCalculationParameters
    ): ExposureData {
        // 1. Group `Exposure Windows by Date`
        val exposureWindowsAndResultForDate = exposureWindowsAndResult
            .filter { it.key.dateMillisSinceEpoch == date }

        // 2. Determine `Normalized Time per Date`
        val normalizedTime = exposureWindowsAndResultForDate.values
            .sumOf { it.normalizedTime }

        Timber.d("normalizedTime: $normalizedTime")

        // 3. Determine `Risk Level per Date`
        val riskLevel = try {
            riskCalculationParameters.normalizedTimePerDayToRiskLevelMappingList
                .filter { it.normalizedTimeRange.inRange(normalizedTime.toLong()) }
                .map { it.riskLevel }
                .first()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid config for normalizedTimePerDayToRiskLevelMapping")
        }

        Timber.d("riskLevel: ${riskLevel.name} (${riskLevel.ordinal})")

        // 4. Determine `Minimum Distinct Encounters With Low Risk per Date`
        val minimumDistinctEncountersWithLowRisk = exposureWindowsAndResultForDate
            .filter { it.value.riskLevel == RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW }
            .map { "${it.value.transmissionRiskLevel}_${it.key.calibrationConfidence}" }
            .distinct()
            .size

        Timber.d("minimumDistinctEncountersWithLowRisk: $minimumDistinctEncountersWithLowRisk")

        // 5. Determine `Minimum Distinct Encounters With High Risk per Date`
        val minimumDistinctEncountersWithHighRisk = exposureWindowsAndResultForDate
            .filter { it.value.riskLevel == RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH }
            .map { "${it.value.transmissionRiskLevel}_${it.key.calibrationConfidence}" }
            .distinct()
            .size

        Timber.d("minimumDistinctEncountersWithHighRisk: $minimumDistinctEncountersWithHighRisk")

        return ExposureData(
            date,
            riskLevel,
            minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk
        )
    }

    private fun RiskCalculationParametersOuterClass.Range.inRange(value: Int): Boolean {
        if (this.minExclusive && value <= this.min) return false
        else if (!this.minExclusive && value < this.min) return false
        if (this.maxExclusive && value >= this.max) return false
        else if (!this.maxExclusive && value > this.max) return false
        return true
    }

    private fun RiskCalculationParametersOuterClass.Range.inRange(value: Float): Boolean {
        if (this.minExclusive && value <= this.min) return false
        else if (!this.minExclusive && value < this.min) return false
        if (this.maxExclusive && value >= this.max) return false
        else if (!this.maxExclusive && value > this.max) return false
        return true
    }

    private fun RiskCalculationParametersOuterClass.Range.inRange(value: Double): Boolean {
        if (this.minExclusive && value <= this.min) return false
        else if (!this.minExclusive && value < this.min) return false
        if (this.maxExclusive && value >= this.max) return false
        else if (!this.maxExclusive && value > this.max) return false
        return true
    }
}

class NormalizedTimePerExposureWindowToRiskLevelMappingMissingException : Exception()
class UnknownReportTypeException : Exception()
