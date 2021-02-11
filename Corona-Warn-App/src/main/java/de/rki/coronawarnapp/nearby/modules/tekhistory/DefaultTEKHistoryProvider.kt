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
            onTEKHistoryAvailable(getTEKHistory())
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

    override suspend fun getTEKHistory(): List<TemporaryExposureKey> {
        val isPreAuthorized = try {
            preAuthorizeExposureKeyHistory()
        } catch (e: Exception) {
            Timber.d(e, "Requesting Pre-Auth failed")
            // Let getTEKHistoryOrRequestPermission handle it
            if (e is ApiException && e.status.hasResolution()) throw e
            false
        }

        return if (isPreAuthorized) {
            try {
                Timber.d("Pre-Auth retrieving temporary exposure keys.")
                getPreAuthorizedExposureKeys().also {
                    Timber.d("Pre-Auth temporary exposure keys:${it.joinToString("\n")}")
                }
            } catch (e: Exception) {
                Timber.d(e, "Pre-Auth retrieving temporary exposure keys failed")
                // Let getTEKHistoryOrRequestPermission handle it
                if (e is ApiException && e.status.hasResolution()) throw e

                Timber.d("Fallback:Retrieving temporary exposure keys")
                client.temporaryExposureKeyHistory.await().also {
                    Timber.d("Temporary exposure keys:${it.joinToString("\n")}")
                }
            }
        } else {
            Timber.d("Retrieving temporary exposure keys")
            client.temporaryExposureKeyHistory.await().also {
                Timber.d("Temporary exposure keys:${it.joinToString("\n")}")
            }
        }
    }

    override suspend fun preAuthorizeExposureKeyHistory(): Boolean {
        // Pre-Auth isn't available exist early
        if (!enfVersion.isAtLeast(ENFVersion.V1_8)) return false
        Timber.i("Requesting Per-Auth TemporaryExposureKeyHistory with v${ENFVersion.V1_8}")
        client.requestPreAuthorizedTemporaryExposureKeyHistory().await()
        Timber.i("Pre-Auth TemporaryExposureKeyHistory is enabled")
        return true
    }

    private suspend fun getPreAuthorizedExposureKeys(): List<TemporaryExposureKey> = suspendCoroutine { cont ->
        client.requestPreAuthorizedTemporaryExposureKeyRelease()
            .addOnSuccessListener {
                Timber.i("Pre-Auth waiting for Keys broadcast event")
                context.registerReceiver(
                    object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            Timber.i("Pre-Auth keys Broadcast received")
                            if (intent.hasExtra(ExposureNotificationClient.EXTRA_TEMPORARY_EXPOSURE_KEY_LIST)) {
                                val tempExposureKeys = intent.getParcelableArrayListExtra<TemporaryExposureKey>(
                                    ExposureNotificationClient.EXTRA_TEMPORARY_EXPOSURE_KEY_LIST
                                )
                                Timber.i("Pre-Auth temporary exposure keys were retrieved:$tempExposureKeys")
                                cont.resume(tempExposureKeys)
                            } else {
                                Timber.w("Pre-Auth temporary exposure keys were not retrieved")
                                cont.resumeWithException(Exception("Pre-Auth temporary exposure keys were not retrieved"))
                            }
                            context.unregisterReceiver(this)
                        }
                    },
                    IntentFilter(
                        ExposureNotificationClient.ACTION_PRE_AUTHORIZE_RELEASE_PHONE_UNLOCKED
                    )
                )
            }.addOnFailureListener {
                Timber.e(it, "Pre-Auth failed to retrieve temporary exposure keys.")
                cont.resumeWithException(it)
            }
    }
}
