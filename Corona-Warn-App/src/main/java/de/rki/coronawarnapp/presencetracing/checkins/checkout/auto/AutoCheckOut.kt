package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.app.AlarmManager
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Knows when the party is over.
 * (╯°□°)╯︵ ┻━┻
 */
@Singleton
class AutoCheckOut @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val repository: CheckInRepository,
    private val checkOutHandler: CheckOutHandler,
    private val alarmManager: AlarmManager,
    private val timeStamper: TimeStamper,
    private val intentFactory: AutoCheckOutIntentFactory,
) : Initializer {

    private val mutex = Mutex()

    override fun initialize() {
        repository.allCheckIns
            .onStart { Timber.tag(TAG).v("Monitoring check-ins.") }
            .distinctUntilChanged()
            .onEach {
                Timber.tag(TAG).i("Check-ins changed, checking for overdue items, refreshing alarm.")
                processOverDueCheckouts()
                refreshAlarm()
            }
            .launchIn(appScope)
    }

    private suspend fun findNextAutoCheckOut(nowUTC: Instant): CheckIn? = repository.allCheckIns
        .firstOrNull()
        ?.filter { !it.completed && it.checkInEnd.isAfter(nowUTC) }
        ?.minByOrNull { it.checkInEnd }

    suspend fun refreshAlarm(): Boolean = mutex.withLock {
        Timber.tag(TAG).d("refreshAlarm()")

        val nowUTC = timeStamper.nowUTC
        // We only create alarms that are in the future
        val nextCheckout = findNextAutoCheckOut(nowUTC)

        return if (nextCheckout != null) {
            Timber.tag(TAG).d(
                "Next check-out will be at %s (in %d min) for %s",
                nextCheckout.checkInEnd,
                Duration.between(nowUTC, nextCheckout.checkInEnd).toMinutes(),
                nextCheckout
            )
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextCheckout.checkInEnd.toEpochMilli(),
                intentFactory.createIntent(nextCheckout.id)
            )
            true
        } else {
            Timber.tag(TAG).d("There is currently no up-coming check-out, canceling alarm.")
            alarmManager.cancel(intentFactory.createIntent())
            false
        }
    }

    suspend fun processOverDueCheckouts(): List<Long> = mutex.withLock {
        Timber.tag(TAG).d("processOverDueCheckouts()")

        val overDueCheckouts = run {
            val nowUTC = timeStamper.nowUTC
            val snapshot = repository.allCheckIns.firstOrNull() ?: emptyList()
            snapshot
                .filter { !it.completed && (nowUTC.isAfter(it.checkInEnd) || nowUTC == it.checkInEnd) }
                .sortedBy { it.checkInEnd }
        }.also {
            Timber.tag(TAG).d("${it.size} checkins are overdue for auto checkout: %s", it)
        }

        val successfulCheckouts = overDueCheckouts.mapNotNull {
            try {
                checkOutHandler.checkOut(checkInId = it.id, it.checkInEnd)
                it
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Checkout for failed on %s", it)
                null
            }
        }
        return successfulCheckouts.map { it.id }
    }

    suspend fun performCheckOut(targetId: Long): Boolean = mutex.withLock {
        Timber.tag(TAG).v("performCheckOut(targetId=$targetId)")

        if (targetId == 0L) {
            Timber.tag(TAG).e("Invalid target checkInId=$targetId")
            return false
        }

        val targetCheckIn = repository.getCheckInById(targetId)?.let {
            if (it.completed) {
                Timber.tag(TAG).w("Target checkIn is already completed: %s", it)
                null
            } else {
                it
            }
        }

        if (targetCheckIn == null) {
            Timber.tag(TAG).w("Checkin with checkInId=$targetId no longer exists!?")
            return false
        }

        return try {
            checkOutHandler.checkOut(checkInId = targetCheckIn.id, targetCheckIn.checkInEnd)
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Checkout for failed on %s", targetCheckIn)
            false
        }
    }

    companion object {
        private const val TAG = "AutoCheckOut"
    }
}
