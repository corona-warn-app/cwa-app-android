package de.rki.coronawarnapp.timer

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SettingsRepository
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import timber.log.Timber
import java.util.Timer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.fixedRateTimer

/**
 * Singleton class for timer handling
 */
object TimerHelper {

    private val TAG: String? = TimerHelper::class.simpleName

    /**
     * Atomic boolean for timer existence check
     *
     * @see AtomicBoolean
     */
    private val isManualKeyRetrievalOnTimer = AtomicBoolean(false)

    /**
     * A timer for manual key retrieval button
     *
     * @see Timer
     */
    private var manualKeyRetrievalTimer: Timer? = null

    /**
     * Manual key retrieval button timer unique name
     */
    private const val MANUAL_KEY_RETRIEVAL_TIMER_NAME = "ManualKeyRetrievalTimer"

    /**
     * Timer tick in milliseconds
     */
    private const val TIMER_TICK = 1000L

    /**
     * Initial timer delay in milliseconds
     */
    private const val INITIAL_TIMER_DELAY = 0L

    /**
     * Get cooldown time left between last time update button was triggered and current time
     *
     * @return Long
     *
     * @see LocalData.lastTimeDiagnosisKeysFromServerFetch
     * @see TimeVariables.getManualKeyRetrievalDelay
     */
    private fun getManualKeyRetrievalTimeLeft(): Long {
        if (LocalData.lastTimeDiagnosisKeysFromServerFetch() == null) return 0

        val currentDate = DateTime(Instant.now(), DateTimeZone.getDefault())
        val lastFetch =
            DateTime(LocalData.lastTimeDiagnosisKeysFromServerFetch(), DateTimeZone.getDefault())

        return TimeVariables.getManualKeyRetrievalDelay() - (currentDate.millis - lastFetch.millis)
    }

    /**
     * Start manual key retrieval timer
     * Update last call time with current time in shared preferences, set the enable flag to false
     * and starts the cooldown timer.
     *
     * @see SettingsRepository.isManualKeyRetrievalEnabled
     */
    fun startManualKeyRetrievalTimer() {
        checkManualKeyRetrievalTimer()
    }

    /**
     * Start manual key retrieval timer if not yet started
     * Every timer tick refresh manual key retrieval button status and text
     *
     * @see isManualKeyRetrievalOnTimer
     * @see MANUAL_KEY_RETRIEVAL_TIMER_NAME
     * @see TIMER_TICK
     */
    fun checkManualKeyRetrievalTimer() {
        if (!isManualKeyRetrievalOnTimer.get() && getManualKeyRetrievalTimeLeft() > 0) {
            try {
                isManualKeyRetrievalOnTimer.set(true)
                manualKeyRetrievalTimer =
                    fixedRateTimer(
                        MANUAL_KEY_RETRIEVAL_TIMER_NAME,
                        true,
                        INITIAL_TIMER_DELAY,
                        TIMER_TICK
                    ) {
                        onManualKeyRetrievalTimerTick()
                    }.also { it.logTimerStart() }
            } catch (e: Exception) {
                logTimerException(e)
            }
        }
        if (!isManualKeyRetrievalOnTimer.get()) {
            SettingsRepository.updateManualKeyRetrievalEnabled(true)
        }
    }

    /**
     * Process manual key retrieval timer tick
     * If no cooldown time left - stop timer, change text and enable update button
     * Else - update text with timer HMS format
     *
     * @see getManualKeyRetrievalTimeLeft
     * @see SettingsRepository.updateManualKeyRetrievalEnabled
     * @see SettingsRepository.updateManualKeyRetrievalTime
     * @see de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHMS
     */
    private fun onManualKeyRetrievalTimerTick() {
        val timeDifference = getManualKeyRetrievalTimeLeft()
        val result = timeDifference <= 0
        SettingsRepository.updateManualKeyRetrievalEnabled(result)
        SettingsRepository.updateManualKeyRetrievalTime(timeDifference)
        if (result) stopManualKeyRetrievalTimer()
    }

    /**
     * Stop manual key retrieval timer and set timer flag to false
     *
     * @see isManualKeyRetrievalOnTimer
     * @see MANUAL_KEY_RETRIEVAL_TIMER_NAME
     */
    private fun stopManualKeyRetrievalTimer() {
        manualKeyRetrievalTimer?.cancel()
        isManualKeyRetrievalOnTimer.set(false)
        logTimerStop(MANUAL_KEY_RETRIEVAL_TIMER_NAME)
    }

    /**
     * Log timer start
     */
    private fun Timer.logTimerStart() {
        if (BuildConfig.DEBUG) Timber.d("Timer started: $this")
    }

    /**
     * Log timer stop
     */
    private fun logTimerStop(timerName: String) {
        if (BuildConfig.DEBUG) Timber.d("Timer stopped: $timerName")
    }

    /**
     * Log timer exception
     */
    private fun logTimerException(exception: java.lang.Exception) {
        Timber.e("Timer exception: $exception")
    }
}
