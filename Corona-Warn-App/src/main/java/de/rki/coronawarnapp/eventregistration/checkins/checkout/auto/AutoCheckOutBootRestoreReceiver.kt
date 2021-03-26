package de.rki.coronawarnapp.eventregistration.checkins.checkout.auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.android.AndroidInjection
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AutoCheckOutBootRestoreReceiver : BroadcastReceiver() {
    @Inject @AppScope lateinit var scope: CoroutineScope
    @Inject lateinit var dispatcherProvider: DispatcherProvider
    @Inject lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).d("onReceive(context=%s, intent=%s)", context, intent)

        if (!EXPECTED_INTENTS.contains(intent.action)) {
            Timber.tag(TAG).e("Received unknown intent action: %s", intent.action)
            return
        }

        AndroidInjection.inject(this, context)

        val async = goAsync()

        scope.launch(context = scope.coroutineContext) {
            try {
                val data = Data.Builder()
                    .putBoolean(AutoCheckOutWorker.ARGKEY_PROCESS_OVERDUE, true)
                    .build()

                OneTimeWorkRequest
                    .Builder(AutoCheckOutWorker::class.java)
                    .setInputData(data)
                    .build()
                    .let { workManager.enqueue(it) }

                Timber.tag(TAG).i("Post boot refresh queued.")
            } catch (e: Exception) {
                e.reportProblem(TAG, "Failed to process intent.")
            } finally {
                Timber.tag(TAG).i("Finished processing broadcast.")
                async.finish()
            }
        }
    }

    companion object {
        private val EXPECTED_INTENTS = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
        private const val TAG = "AutoCheckoutReceiver"
    }
}
