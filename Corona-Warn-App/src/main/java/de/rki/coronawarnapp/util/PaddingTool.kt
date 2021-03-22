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
    // Common
    /**
     * Generates a random string with passed [length]
     * Passing characters [A-Z], [a-z] and [0-9]
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
        val keyCount = max(MIN_KEY_COUNT_FOR_SUBMISSION - keyListSize, 0)
        return requestPadding(KEY_SIZE * keyCount)
    }

    private const val MIN_KEY_COUNT_FOR_SUBMISSION = 14
    private const val KEY_SIZE = 28 // 28 bytes per key
    // ---------- CheckIn padding ----------

    val NumberOfFakeCheckInsFunctionParametersOrBuilder.equation: (Double) -> Double
        // f(x) = p * q ^ r*(s*(x+t))^u  + ax^2 + bx + c
        get() = { x ->
            val exponent = r * (s * (x + t)).pow(u)
            p * q.pow(exponent) + a * x.pow(2.0) + b * x + c
        }

    fun PlausibleDeniabilityParametersContainer.determineNumberOfFakeCheckIns(
        numberOfLocalCheckIns: Int
    ): Double {
        Timber.i("Starting determineNumberOfFakeCheckIns ...")
        val random = Random.asJavaRandom() // Kotlin does not implement [nextGaussian]
        val probabilityThreshold: Double = if (numberOfLocalCheckIns == 0) {
            probabilityToFakeCheckInsIfNoCheckIns
        } else {
            probabilityToFakeCheckInsIfSomeCheckIns
        }
        Timber.i("probabilityThreshold=$probabilityThreshold")

        val randomUniformNumber = Math.random()
        Timber.i("randomUniformNumber=$randomUniformNumber")

        if (randomUniformNumber > probabilityThreshold) return 0.0

        val x = random.nextGaussian()
        Timber.i("x=$x")

        val equationParameters = numberOfFakeCheckInsFunctionParameters.firstOrNull { functionParam ->
            functionParam.randomNumberRange.inRange(x)
        } ?: return 0.0
        Timber.i("equationParameters=$equationParameters")

        return equationParameters.equation(x)
    }

    fun checkInPadding(
        parameters: PlausibleDeniabilityParametersContainer,
        numberOfLocalCheckIns: Int
    ): String {
        Timber.i("Starting checkInPadding ...")
        val checkInBytesSizes: List<Int> = parameters.checkInSizesInBytes
        if (checkInBytesSizes.isEmpty()) return requestPadding(0)

        val fakeCheckInsNumber: Int = parameters.determineNumberOfFakeCheckIns(numberOfLocalCheckIns).roundToInt()
        val numberOfBytes: Int = (0 until fakeCheckInsNumber)
            .map {
                val index = floor(Math.random() * checkInBytesSizes.size).toInt()
                checkInBytesSizes[index]
            }
            .reduce { sum, size ->
                sum + size
            }
        return requestPadding(numberOfBytes)
    }
}
