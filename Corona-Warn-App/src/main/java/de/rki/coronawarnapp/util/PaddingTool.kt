package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.risk.DefaultRiskLevels.Companion.inRange
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParametersOrBuilder

import de.rki.coronawarnapp.submission.server.SubmissionServer
import java.util.Random
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

object PaddingTool {
    // ---------- Key padding ----------
    /**
     * Temporary exposure keys request padding for [SubmissionServer]
     */
    fun keyPadding(length: Int): String = (1..length)
        .map { PADDING_ITEMS.random() }
        .joinToString("")

    private val PADDING_ITEMS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    // ---------- CheckIn padding ----------

    private val NumberOfFakeCheckInsFunctionParametersOrBuilder.equation: (Double) -> Double
        // f(x) = p * q ^ r*(s*(x+t))^u  + ax^2 + bx + c
        get() = { x ->
            val exponent = r * (s * (x + t)).pow(u)
            p * q.pow(exponent) + a * x.pow(2.0) + b * x + c
        }

    private fun PlausibleDeniabilityParametersContainer.determineNumberOfFakeCheckIns(
        numberOfLocalCheckIns: Int
    ): Double {
        val random = Random() // Java Random class
        val probabilityThreshold = if (numberOfLocalCheckIns == 0) {
            probabilityToFakeCheckInsIfNoCheckIns
        } else {
            probabilityToFakeCheckInsIfSomeCheckIns
        }

        if (random.nextDouble() > probabilityThreshold) return 0.0

        val x = random.nextGaussian()

        val equationParameters = numberOfFakeCheckInsFunctionParameters.firstOrNull { functionParam ->
            functionParam.randomNumberRange.inRange(x)
        } ?: return 0.0

        return equationParameters.equation(x)
    }

    fun checkInPadding(
        parameters: PlausibleDeniabilityParametersContainer,
        numberOfLocalCheckIns: Int
    ): String {

        val checkInSizesInBytes: List<Int> = parameters.checkInSizesInBytes
        if (checkInSizesInBytes.isEmpty()) return keyPadding(0)

        val numberOfFakeCheckIns: Int = parameters.determineNumberOfFakeCheckIns(numberOfLocalCheckIns).roundToInt()
        val numberOfBytes: Int = (0 until numberOfFakeCheckIns)
            .map {
                val index = floor(Math.random() * checkInSizesInBytes.size).toInt()
                checkInSizesInBytes[index]
            }
            .reduce { sum, bytes ->
                sum + bytes
            }
        return keyPadding(numberOfBytes)
    }
}
