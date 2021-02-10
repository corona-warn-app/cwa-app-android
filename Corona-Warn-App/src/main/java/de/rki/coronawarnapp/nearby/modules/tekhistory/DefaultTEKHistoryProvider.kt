package de.rki.coronawarnapp.nearby.modules.tekhistory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultTEKHistoryProvider @Inject constructor(
    private val client: ExposureNotificationClient,
    private val enfVersion: ENFVersion,
    @AppContext private val context: Context
) : TEKHistoryProvider {

    override suspend fun getTEKHistoryOrRequestPermission(
        onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit,
        onPermissionRequired: (Status) -> Unit
    ) {
        Timber.d("getTEKHistoryOrRequestPermission(...)")
        try {
            if (enfVersion.isAtLeast(ENFVersion.V1_8)) {
                Timber.d("Request pre-auth TemporaryKeyHistory")
                try {
                    getTEKPreAuthorizedHistory(onTEKHistoryAvailable)
                } catch (exception: Exception) {
                    if (exception is ApiException &&
                        exception.status.hasResolution()
                    ) {
                        Timber.e(exception, "Request pre-auth TemporaryKeyHistory failed with recoverable error")
                        throw exception
                    } else {
                        Timber.e(exception, "Request pre-auth TemporaryKeyHistory failed with unrecoverable error")
                        Timber.d("Fallback request non-auth TemporaryKeyHistory")
                        onTEKHistoryAvailable(getTEKHistory())
                    }
                }
            } else {
                Timber.d("Request non-auth TemporaryKeyHistory")
                onTEKHistoryAvailable(getTEKHistory())
            }

            Timber.d("onTEKHistoryAvailable() -> permission were already available")
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
        Timber.d("Retrieving temporary exposure keys.")
        client.temporaryExposureKeyHistory
            .addOnSuccessListener {
                Timber.d("Temporary exposure keys were retrieved: %s", it.joinToString("\n"))
                cont.resume(it)
            }
            .addOnFailureListener {
                Timber.w(it, "Failed to retrieve temporary exposure keys.")
                cont.resumeWithException(it)
            }
    }

    override suspend fun preAuthorizeExposureKeyHistory(): Boolean {
        // Early exist if pre-auth is not available
        if (!enfVersion.isAtLeast(ENFVersion.V1_8)) return false
        Timber.i("Per-Auth TemporaryExposureKeyHistory with v${ENFVersion.V1_8}")
        client.requestPreAuthorizedTemporaryExposureKeyHistory().await()
        Timber.i("Pre-auth is enabled")
        return true
    }

    private suspend fun getTEKPreAuthorizedHistory(onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit) {
        client.requestPreAuthorizedTemporaryExposureKeyRelease().await()
        Timber.i("requestPreAuthorizedTemporaryExposureKeyRelease is done")
        Timber.i("Wait for KeyBroadcastsEvent")
        context.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Timber.i("Key Broadcast received")
                    if (intent.hasExtra(ExposureNotificationClient.EXTRA_TEMPORARY_EXPOSURE_KEY_LIST)) {
                        Timber.i("Key Broadcast has EXTRA for keys")
                        val tempExposureKeys = intent.getParcelableArrayListExtra<TemporaryExposureKey>(
                            ExposureNotificationClient.EXTRA_TEMPORARY_EXPOSURE_KEY_LIST
                        )
                        onTEKHistoryAvailable(tempExposureKeys)
                        Timber.i("Pre-auth temporary exposure keys were retrieved:$tempExposureKeys")
                    }
                    context.unregisterReceiver(this)
                }
            },
            IntentFilter(
                ExposureNotificationClient.ACTION_PRE_AUTHORIZE_RELEASE_PHONE_UNLOCKED
            )
        )
    }
}
