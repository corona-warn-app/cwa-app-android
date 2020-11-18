package de.rki.coronawarnapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.ACTION_EXPOSURE_NOT_FOUND
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.EXTRA_TOKEN
import dagger.android.AndroidInjection
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.NoTokenException
import de.rki.coronawarnapp.exception.UnknownBroadcastException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ExposureStateUpdateWorker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection.Result
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Receiver to listen to the Exposure Notification Exposure State Updated event. This event will be triggered from the
 * Google Exposure Notification API whenever the app is providing new diagnosis keys to the API and the
 * new keys are processed. Then the [ExposureStateUpdateReceiver] will receive the corresponding action in its
 * [onReceive] function.
 *
 * Inside this receiver no further action or calculation will be done but it is rather used to inform the
 * [de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction] that the processing of the diagnosis keys is
 * finished and the Exposure Summary can be retrieved in order to calculate a risk level to show to the user.
 *
 * @see de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
 *
 */
class ExposureStateUpdateReceiver : BroadcastReceiver() {

    @Inject @AppScope lateinit var scope: CoroutineScope
    @Inject lateinit var dispatcherProvider: DispatcherProvider
    @Inject lateinit var exposureDetectionTracker: ExposureDetectionTracker
    @Inject lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).d("onReceive(context=%s, intent=%s)", context, intent)
        AndroidInjection.inject(this, context)

        val action = intent.action
        Timber.tag(TAG).v("Looking up action: %s", action)

        val async = goAsync()

        scope.launch(context = scope.coroutineContext) {
            try {
                val token = intent.requireToken()

                trackDetection(token, action)

                val data = Data
                    .Builder()
                    .putString(EXTRA_TOKEN, token)
                    .build()

                OneTimeWorkRequest
                    .Builder(ExposureStateUpdateWorker::class.java)
                    .setInputData(data)
                    .build()
                    .let { workManager.enqueue(it) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process intent.")
                e.report(INTERNAL)
            } finally {
                Timber.tag(TAG).i("Finished processing broadcast.")
                async.finish()
            }
        }
    }

    private fun trackDetection(token: String, action: String?) {
        when (action) {
            ACTION_EXPOSURE_STATE_UPDATED -> {
                exposureDetectionTracker.finishExposureDetection(token, Result.UPDATED_STATE)
            }
            ACTION_EXPOSURE_NOT_FOUND -> {
                exposureDetectionTracker.finishExposureDetection(token, Result.NO_MATCHES)
            }
            else -> throw UnknownBroadcastException(action)
        }
    }

    private fun Intent.requireToken(): String = getStringExtra(EXTRA_TOKEN).also {
        Timber.tag(TAG).v("Extracted token: %s", it)
    } ?: throw NoTokenException(IllegalArgumentException("no token was found in the intent"))

    companion object {
        private val TAG: String? = ExposureStateUpdateReceiver::class.simpleName
    }
}
