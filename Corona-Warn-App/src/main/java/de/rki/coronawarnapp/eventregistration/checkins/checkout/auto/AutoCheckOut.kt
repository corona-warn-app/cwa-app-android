package de.rki.coronawarnapp.eventregistration.checkins.checkout.auto

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Knows when the party is over.
 * (╯°□°)╯︵ ┻━┻
 */
@Singleton
class AutoCheckOut @Inject constructor(
    @AppContext private val context: Context,
    private val repository: CheckInRepository,
    private val checkOutHandler: CheckOutHandler,
    private val alarmManager: AlarmManager,
    private val timeStamper: TimeStamper,
) {

    private fun createIntent(checkInId: Long? = null): PendingIntent {
        val updateServiceIntent = Intent(context, AutoCheckOutBootRestoreReceiver::class.java).apply {
            if (checkInId != null) {
                putExtra(AutoCheckOutReceiver.ARGKEY_RECEIVER_CHECKIN_ID, checkInId)
            }
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, updateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private suspend fun findNextAutoCheckOut(): CheckIn? = repository.allCheckIns
        .firstOrNull()
        ?.filter { !it.completed }
        ?.minByOrNull { it.checkInEnd }

    suspend fun refreshAlarm(): Boolean {
        Timber.tag(TAG).d("refreshAlarm()")

        val nextCheckout = findNextAutoCheckOut()

        return if (nextCheckout != null) {
            Timber.tag(TAG).d("Updating next check-out alarm for %s", nextCheckout)
            val targetTime = nextCheckout.checkInEnd.millis
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTime, createIntent(nextCheckout.id))
            true
        } else {
            Timber.tag(TAG).d("There is currently no up-coming check-out, canceling alarm.")
            alarmManager.cancel(createIntent())
            false
        }
    }

    suspend fun processOverDueCheckouts(): List<Long> {
        Timber.tag(TAG).d("processOverDueCheckouts()")

        val overDueCheckouts = run {
            val nowUTC = timeStamper.nowUTC
            val snapshot = repository.allCheckIns.firstOrNull() ?: emptyList()
            snapshot
                .filter { !it.completed && nowUTC.isAfter(it.checkInEnd) }
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

    suspend fun performCheckOut(targetId: Long): Boolean {
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
        private const val REQUEST_CODE = 5410
        private const val TAG = "AutoCheckOut"
    }
}
