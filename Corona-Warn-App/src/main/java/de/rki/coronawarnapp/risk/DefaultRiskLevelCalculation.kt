package de.rki.coronawarnapp.risk

import android.text.TextUtils
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureData
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

@Singleton
class DefaultRiskLevelCalculation @Inject constructor() : RiskLevelCalculation {

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

    override fun calculateRisk(
        exposureWindow: ExposureWindow,
        riskCalculationParameters: RiskCalculationParametersOuterClass.RiskCalculationParameters
    ): RiskResult {
        TODO("Not yet implemented")
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
                .filter { inRange(it.normalizedTimeRange, normalizedTime.toLong()) }
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

    private fun inRange(range: RiskCalculationParametersOuterClass.Range, value: Long): Boolean {
        if (range.minExclusive && value <= range.min) return false
        else if (!range.minExclusive && value < range.min) return false
        if (range.maxExclusive && value >= range.max) return false
        else if (!range.maxExclusive && value > range.max) return false
        return true
    }
}
