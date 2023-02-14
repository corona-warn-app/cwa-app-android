package de.rki.coronawarnapp.nearby.modules.tekhistory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import androidx.annotation.VisibleForTesting
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.ACTION_PRE_AUTHORIZE_RELEASE_PHONE_UNLOCKED
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.EXTRA_TEMPORARY_EXPOSURE_KEY_LIST
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
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
    @ApplicationContext private val context: Context
) : TEKHistoryProvider {

    override suspend fun getTEKHistoryOrRequestPermission(
        onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit,
        onPermissionRequired: (Status) -> Unit
    ) {
        if (enfVersion.isAtLeast(ENFVersion.V1_8)) {
            getPreAuthTEKHistoryOrRequestPermissionOnV18(
                onTEKHistoryAvailable,
                onPermissionRequired
            )
        } else {
            getTEKHistoryOrRequestPermissionOnPreV18(
                onTEKHistoryAvailable,
                onPermissionRequired
            )
        }
    }

    override suspend fun getTEKHistory(): List<TemporaryExposureKey> {
        return if (enfVersion.isAtLeast(ENFVersion.V1_8)) {
            getPreAuthTEKHistoryOnV18()
        } else {
            getTEKHistoryOnPreV18()
        }
    }

    override suspend fun preAuthorizeExposureKeyHistory(): Boolean {
        // Pre-Auth isn't available exit early
        if (!enfVersion.isAtLeast(ENFVersion.V1_8)) return false
        Timber.i("Requesting Per-Auth TemporaryExposureKeyHistory with v${ENFVersion.V1_8}")
        client.requestPreAuthorizedTemporaryExposureKeyHistory().await()
        Timber.i("Pre-Auth TemporaryExposureKeyHistory is enabled")
        return true
    }

    // Get History or request Permissions //
    private suspend fun getPreAuthTEKHistoryOrRequestPermissionOnV18(
        onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit,
        onPermissionRequired: (Status) -> Unit
    ) {
        Timber.d("getPreAuthTEKHistoryOrRequestPermissionOnV18")
        try {
            onTEKHistoryAvailable(getPreAuthTEKHistoryOnV18())
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

    private suspend fun getTEKHistoryOrRequestPermissionOnPreV18(
        onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit,
        onPermissionRequired: (Status) -> Unit
    ) {
        Timber.d("getTEKHistoryOrRequestPermissionOnPreV18(...)")
        try {
            onTEKHistoryAvailable(getTEKHistoryOnPreV18())
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

    // Get history //
    private suspend fun getTEKHistoryOnPreV18(): List<TemporaryExposureKey> = suspendCoroutine { cont ->
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

    private suspend fun getPreAuthTEKHistoryOnV18(): List<TemporaryExposureKey> {
        return try {
            Timber.d("Pre-Auth retrieving TEK.")
            getPreAuthorizedExposureKeys().also {
                Timber.d("Pre-Auth TEK: %s", it.joinToString("\n"))
            }
        } catch (exception: Exception) {
            Timber.d(exception, "Pre-Auth retrieving TEK failed")
            if (exception.isResolvable) throw exception

            Timber.d("Fallback: Retrieving TEK on pre v1.8")
            getTEKHistoryOnPreV18()
        }
    }

    // Timeout after 5 sec if receiver did not get called
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun getPreAuthorizedExposureKeys(): List<TemporaryExposureKey> = withTimeout(5_000) {
        coroutineScope {
            // Register receiver before hitting the API to avoid race conditions
            val deferredIntent = async { awaitReceivedBroadcast() }
            client.requestPreAuthorizedTemporaryExposureKeyRelease().await()
            Timber.i("Pre-Auth requestPreAuthorizedTemporaryExposureKeyRelease is done")
            val startTime = System.currentTimeMillis()
            Timber.i("Pre-Auth Receiver StartTime:$startTime")
            val intent = deferredIntent.await()
            val endTime = System.currentTimeMillis()
            Timber.i("Pre-Auth Receiver EndTime:$endTime")
            Timber.i("Pre-Auth Receiver WaitingTime:${endTime - startTime}")
            Timber.d("Pre-Auth getPreAuthorizedExposureKeys():intent=%s", intent)
            intent.getParcelableArrayListExtra(EXTRA_TEMPORARY_EXPOSURE_KEY_LIST) ?: emptyList()
        }
    }

    private suspend fun awaitReceivedBroadcast(): Intent = suspendCancellableCoroutine { cont ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                cont.resume(intent)
                Timber.d("Pre-Auth unregisterReceiver")
                context.unregisterReceiver(this)
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(ACTION_PRE_AUTHORIZE_RELEASE_PHONE_UNLOCKED)
        )
        cont.invokeOnCancellation {
            Timber.d(it, "Pre-Auth  unregisterReceiver")
            context.unregisterReceiver(receiver)
        }
    }

    private inline val Exception.isResolvable get() = this is ApiException && this.status.hasResolution()
}
