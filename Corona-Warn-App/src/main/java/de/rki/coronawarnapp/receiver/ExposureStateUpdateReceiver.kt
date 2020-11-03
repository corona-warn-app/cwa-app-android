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
import de.rki.coronawarnapp.nearby.modules.calculationtracker.Calculation
import de.rki.coronawarnapp.nearby.modules.calculationtracker.CalculationTracker
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
    @Inject lateinit var calculationTracker: CalculationTracker
    lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).d("onReceive(context=%s, intent=%s)", context, intent)
        AndroidInjection.inject(this, context)
        this.context = context

        val action = intent.action
        Timber.tag(TAG).v("Looking up action: %s", action)

        val async = goAsync()
        scope.launch(context = dispatcherProvider.Default) {
            try {
                when (action) {
                    ACTION_EXPOSURE_STATE_UPDATED -> processStateUpdates(intent)
                    ACTION_EXPOSURE_NOT_FOUND -> processNotFound(intent)
                    else -> throw UnknownBroadcastException(action)
                }
            } catch (e: Exception) {
                e.report(INTERNAL)
            } finally {
                Timber.tag(TAG).i("Finished processing broadcast.")
                async.finish()
            }
        }
    }

    private fun processStateUpdates(intent: Intent) {
        Timber.tag(TAG).i("Processing ACTION_EXPOSURE_STATE_UPDATED")

        val workManager = WorkManager.getInstance(context)

        val token = intent.requireToken()

        val data = Data
            .Builder()
            .putString(EXTRA_TOKEN, token)
            .build()

        OneTimeWorkRequest
            .Builder(ExposureStateUpdateWorker::class.java)
            .setInputData(data)
            .build()
            .let { workManager.enqueue(it) }

        calculationTracker.finishCalculation(
            token,
            Calculation.Result.UPDATED_STATE
        )
    }

    private fun processNotFound(intent: Intent) {
        Timber.tag(TAG).i("Processing ACTION_EXPOSURE_NOT_FOUND")

        val token = intent.requireToken()

        calculationTracker.finishCalculation(
            token,
            Calculation.Result.NO_MATCHES
        )
    }

    private fun Intent.requireToken(): String = getStringExtra(EXTRA_TOKEN).also {
        Timber.tag(TAG).v("Extracted token: %s", it)
    } ?: throw NoTokenException(IllegalArgumentException("no token was found in the intent"))

    companion object {
        private val TAG: String? = ExposureStateUpdateReceiver::class.simpleName
    }
}
