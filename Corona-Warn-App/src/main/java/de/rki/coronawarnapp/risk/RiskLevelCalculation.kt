package de.rki.coronawarnapp.risk

import android.util.Log
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import kotlin.math.round

object RiskLevelCalculation {
    private var TAG = RiskLevelCalculation::class.simpleName

    private const val DECIMAL_MULTIPLIER = 100

    fun calculateRiskScore(
        attenuationParameters: ApplicationConfigurationOuterClass.AttenuationDuration,
        exposureSummary: ExposureSummary
    ): Double {

        /** all attenuation values are capped to [TimeVariables.MAX_ATTENUATION_DURATION] */
        val weightedAttenuationLow =
            attenuationParameters.weights.low.capped()
                .times(exposureSummary.attenuationDurationsInMinutes[0])
        val weightedAttenuationMid =
            attenuationParameters.weights.mid.capped()
                .times(exposureSummary.attenuationDurationsInMinutes[1])
        val weightedAttenuationHigh =
            attenuationParameters.weights.high.capped()
                .times(exposureSummary.attenuationDurationsInMinutes[2])

        val maximumRiskScore = exposureSummary.maximumRiskScore.toDouble()

        val defaultBucketOffset = attenuationParameters.defaultBucketOffset.toDouble()
        val normalizationDivisor = attenuationParameters.riskScoreNormalizationDivisor.toDouble()

        Log.v(
            TAG,
            "Weighted Attenuation: ($weightedAttenuationLow +" +
                    " $weightedAttenuationMid +" +
                    " $weightedAttenuationHigh +" +
                    " $defaultBucketOffset)"
        )

        val weightedAttenuationDuration =
            weightedAttenuationLow
                .plus(weightedAttenuationMid)
                .plus(weightedAttenuationHigh)
                .plus(defaultBucketOffset)

        Log.v(
            TAG,
            "Formula used: ($maximumRiskScore / $normalizationDivisor) * $weightedAttenuationDuration"
        )

        val riskScore = (maximumRiskScore / normalizationDivisor) * weightedAttenuationDuration

        return round(riskScore.times(DECIMAL_MULTIPLIER)).div(DECIMAL_MULTIPLIER)
    }

    private fun Double.capped(): Double {
        return if (this > TimeVariables.getMaxAttenuationDuration()) {
            TimeVariables.getMaxAttenuationDuration()
        } else {
            this
        }
    }
}
