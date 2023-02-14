package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AutoCheckOutReceiver : BroadcastReceiver() {
    @Inject @AppScope lateinit var scope: CoroutineScope
    @Inject lateinit var dispatcherProvider: DispatcherProvider
    @Inject lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive(context=%s, intent=%s)", context, intent)

        if (ACTION_AUTO_CHECKOUT != intent.action) {
            Timber.e("Received unknown intent action: %s", intent.action)
            return
        }

        val async = goAsync()

        scope.launch(context = scope.coroutineContext) {
            try {
                val checkInId = intent.getLongExtra(ARGKEY_RECEIVER_CHECKIN_ID, 0)

                val data = Data.Builder()
                    .putLong(AutoCheckOutWorker.ARGKEY_CHECKIN_ID, checkInId)
                    .putBoolean(AutoCheckOutWorker.ARGKEY_PROCESS_OVERDUE, true)
                    .build()

                OneTimeWorkRequest
                    .Builder(AutoCheckOutWorker::class.java)
                    .setInputData(data)
                    .build()
                    .let { workManager.enqueue(it) }

                Timber.i("AutoCheckoutWorker queued for Check-in#$checkInId")
            } catch (e: Exception) {
                e.reportProblem("AutoCheckOutReceiver", "Failed to process intent.")
            } finally {
                Timber.i("Finished processing broadcast.")
                async.finish()
            }
        }
    }

    companion object {
        const val ARGKEY_RECEIVER_CHECKIN_ID = "autoCheckout.checkInId"
        const val ACTION_AUTO_CHECKOUT = "de.rki.coronawarnapp.intent.action.AUTO_CHECKOUT"
    }
}
