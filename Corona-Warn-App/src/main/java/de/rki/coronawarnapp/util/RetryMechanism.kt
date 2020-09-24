package de.rki.coronawarnapp.util

import timber.log.Timber
import kotlin.math.pow
import kotlin.math.roundToLong

object RetryMechanism {

    fun <T> retryWithBackOff(
        delayCalculator: (Attempt) -> Long? = createDelayCalculator(),
        delayOperation: (Long) -> Unit = { Thread.sleep(it) },
        action: () -> T
    ): T {
        var current = Attempt()
        while (true) {
            Timber.v("Executing attempt: %s", current)
            try {
                return action()
            } catch (e: Exception) {
                current = current.copy(exception = e)
            }

            val newDelay = delayCalculator(current)

            if (newDelay == null) {
                Timber.w("Retrycondition exceeded: %s", current)
                throw current.exception!!
            } else {
                delayOperation(newDelay)
            }

            current = current.copy(
                count = current.count + 1,
                lastDelay = newDelay,
                totalDelay = current.totalDelay + newDelay
            )
        }
    }

    fun createDelayCalculator(
        maximumDelay: Long = 3 * 1000L, // 3 seconds max between retries
        minimumDelay: Long = 0, // Immediate retry
        multiplier: Double = 1.0,
    ): (Attempt) -> Long? = { attempt ->
        if (attempt.totalDelay > 10 * 1000L) {
            Timber.w("Max retry duration exceeded.")
            null
        } else {
            val exp = 2.0.pow(attempt.count.toDouble())
            val calculatedDelay = (multiplier * exp).roundToLong()
            calculatedDelay.coerceAtMost(maximumDelay).coerceAtLeast(minimumDelay)
        }
    }

    data class Attempt(
        val count: Int = 1,
        val totalDelay: Long = 0L,
        val lastDelay: Long = 0L,
        val exception: Exception? = null
    )
}
