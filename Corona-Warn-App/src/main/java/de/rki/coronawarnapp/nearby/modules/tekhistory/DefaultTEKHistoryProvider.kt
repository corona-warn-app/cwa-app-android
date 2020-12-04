package de.rki.coronawarnapp.nearby.modules.tekhistory

import android.content.IntentSender
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultTEKHistoryProvider @Inject constructor(
    private val client: ExposureNotificationClient
) : TEKHistoryProvider {

    override suspend fun isTEKAccessPermissionGranted(): Boolean {
        return try {
            getTEKHistory()
            true
        } catch (e: ApiException) {
            if (e.statusCode == ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
                false
            } else {
                throw e
            }
        }
    }

    override suspend fun getTEKHistoryOrRequestPermission(
        onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit,
        onPermissionRequired: (Status) -> Unit
    ) {
        try {
            onTEKHistoryAvailable(getTEKHistory())
        } catch (apiException: ApiException) {
            if (apiException.statusCode != ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
                throw apiException
            }
            if (!apiException.status.hasResolution()) {
                throw apiException
            }
            try {
                onPermissionRequired(apiException.status)
            } catch (e: IntentSender.SendIntentException) {
                throw e
            }
        }
    }

    override suspend fun getTEKHistory(): List<TemporaryExposureKey> = suspendCoroutine { cont ->
        Timber.i("Retrieving temporary exposure keys.")
        client.temporaryExposureKeyHistory
            .addOnSuccessListener {
                Timber.i("Temporary exposure keys were retrieved: %s", it.joinToString("\n"))
                cont.resume(it)
            }
            .addOnFailureListener {
                Timber.e(it, "Failed to retrieve temporary exposure keys.")
                cont.resumeWithException(it)
            }
    }
}
