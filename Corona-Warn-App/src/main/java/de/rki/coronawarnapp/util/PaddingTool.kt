package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.risk.DefaultRiskLevels.Companion.inRange
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
    .PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParametersOrBuilder

import de.rki.coronawarnapp.submission.server.SubmissionServer
import timber.log.Timber
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.asJavaRandom

object PaddingTool {
    // Common padding

    /**
     * Generates a random string with passed [length]
     * Padding characters [A-Z], [a-z] and [0-9]
     */
    fun requestPadding(length: Int): String = (1..length)
        .map { PADDING_ITEMS.random() }
        .joinToString("")

    private val PADDING_ITEMS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    // ---------- Key padding ----------

    /**
     * Temporary exposure keys request padding for [SubmissionServer]
     */
    fun keyPadding(keyListSize: Int): String {
        Timber.d("keyPadding(keyListSize=$keyListSize)")
        val keyCount = max(MIN_KEY_COUNT_FOR_SUBMISSION - keyListSize, 0)
        return requestPadding(KEY_SIZE * keyCount)
    }

    private const val MIN_KEY_COUNT_FOR_SUBMISSION = 15 // Increased from 14 to 15 in purpose for CheckIn submission
    private const val KEY_SIZE = 28 // 28 bytes per key

    // ---------- CheckIn padding ----------

    val NumberOfFakeCheckInsFunctionParametersOrBuilder.equation: (Double) -> Double
        // f(x) = p * q ^ r*(s*(x+t))^u  + ax^2 + bx + c
        get() = { x ->
            val exponent = r * (s * (x + t)).pow(u)
            p * q.pow(exponent) + a * x.pow(2.0) + b * x + c
        }

    fun PlausibleDeniabilityParametersContainer.determineFakeCheckInsNumber(
        checkInListSize: Int
    ): Double {
        Timber.d("determineFakeCheckInsNumber(checkInListSize=$checkInListSize)")
        val probabilityThreshold: Double = if (checkInListSize == 0) {
            probabilityToFakeCheckInsIfNoCheckIns
        } else {
            probabilityToFakeCheckInsIfSomeCheckIns
        }
        Timber.d("probabilityThreshold=$probabilityThreshold")

        val randomUniformNumber = Math.random()
        Timber.d("randomUniformNumber=$randomUniformNumber")

        if (randomUniformNumber > probabilityThreshold) return 0.0

        // Kotlin doesn't implement [nextGaussian]
        val x = Random.asJavaRandom().nextGaussian()
        Timber.d("x=$x")

        val equationParameters = numberOfFakeCheckInsFunctionParameters.firstOrNull { functionParam ->
            functionParam.randomNumberRange.inRange(x)
        } ?: return 0.0
        Timber.d("equationParameters=$equationParameters")

        return equationParameters.equation(x)
    }

    /**
     * Returns check-in padding for [SubmissionServer]
     */
    fun checkInPadding(
        plausibleParameters: PlausibleDeniabilityParametersContainer,
        checkInListSize: Int
    ): String {
        Timber.d("checkInPadding(plausibleParameters=$plausibleParameters, checkInListSize=$checkInListSize)")

        val checkInBytesSizes: List<Int> = plausibleParameters.checkInSizesInBytes
        Timber.d("checkInBytesSizes=$checkInBytesSizes")

        if (checkInBytesSizes.isEmpty()) return requestPadding(0)

        val fakeCheckInsNumber: Int = plausibleParameters.determineFakeCheckInsNumber(checkInListSize).roundToInt()
        val numberOfBytes: Int = (0 until fakeCheckInsNumber)
            .map {
                val index = floor(Math.random() * checkInBytesSizes.size).toInt()
                checkInBytesSizes[index]
            }
            .fold(initial = 0) { sum, size ->
                sum + size
            }
        return requestPadding(numberOfBytes)
    }
}
