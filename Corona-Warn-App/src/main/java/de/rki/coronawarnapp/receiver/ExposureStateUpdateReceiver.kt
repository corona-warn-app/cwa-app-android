package de.rki.coronawarnapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.NoTokenException
import de.rki.coronawarnapp.exception.WrongReceiverException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ExposureStateUpdateWorker

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
    companion object {
        private val TAG: String? = ExposureStateUpdateReceiver::class.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val expectedAction = ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
        try {
            if (expectedAction != action) {
                throw WrongReceiverException(
                    action,
                    expectedAction,
                    IllegalArgumentException("wrong action was received")
                )
            }

            val token =
                intent.getStringExtra(ExposureNotificationClient.EXTRA_TOKEN)
                    ?: throw NoTokenException(
                        IllegalArgumentException("no token was found in the intent")
                    )

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(
                OneTimeWorkRequest.Builder(ExposureStateUpdateWorker::class.java)
                    .setInputData(
                        Data.Builder()
                            .putString(ExposureNotificationClient.EXTRA_TOKEN, token)
                            .build()
                    )
                    .build()
            )
        } catch (e: WrongReceiverException) {
            e.report(INTERNAL)
        } catch (e: NoTokenException) {
            e.report(INTERNAL)
        }
    }
}
