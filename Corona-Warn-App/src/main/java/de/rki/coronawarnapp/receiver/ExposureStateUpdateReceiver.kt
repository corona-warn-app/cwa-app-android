package de.rki.coronawarnapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.ExceptionCategory.EXPOSURENOTIFICATION
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.WrongReceiverException
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

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
        private const val EXPOSURE_STATE_UPDATE_PERMISSION =
            "com.google.android.gms.nearby.exposurenotification.EXPOSURE_CALLBACK"
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
            val token = LocalData.googleApiToken()

            val pendingRepository = goAsync()

            GlobalScope.launch(Dispatchers.Default) {
                try {
                    val exposureSummary = InternalExposureNotificationClient
                        .asyncGetExposureSummary(token ?: UUID.randomUUID().toString())

                    ExposureSummaryRepository.getExposureSummaryRepository()
                        .insertExposureSummaryEntity(exposureSummary)
                    pendingRepository.finish()
                    Log.v(TAG, "exposure summary state updated")
                    try {
                        RiskLevelTransaction.start()
                    } catch (e: TransactionException) {
                        e.report(INTERNAL)
                    }
                    Log.v(TAG, "exposure summary updated - trigger a new risk level calculation")
                    Log.v(TAG, exposureSummary.toString())
                } catch (e: ApiException) {
                    e.report(EXPOSURENOTIFICATION)
                }
            }
        } catch (e: WrongReceiverException) {
            e.report(INTERNAL)
        }
    }
}
