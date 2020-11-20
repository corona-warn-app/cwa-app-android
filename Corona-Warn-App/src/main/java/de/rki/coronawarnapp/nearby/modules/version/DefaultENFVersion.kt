package de.rki.coronawarnapp.nearby.modules.version

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultENFVersion @Inject constructor(
    private val client: ExposureNotificationClient
) : ENFVersion {

    override suspend fun getENFClientVersion(): Long? = try {
        internalGetENFClientVersion()
    } catch (e: Exception) {
        Timber.w(e, "Failed to get ENFClient version.")
        null
    }

    override suspend fun requireMinimumVersion(required: Long) {
        try {
            val currentVersion = internalGetENFClientVersion()
            if (currentVersion < required) {
                val error = OutdatedENFVersionException(current = currentVersion, required = required)
                Timber.e(error, "Version requirement not satisfied.")
                throw error
            } else {
                Timber.d("Version requirement satisfied: current=$currentVersion, required=$required")
            }
        } catch (apiException: ApiException) {
            if (apiException.statusCode != CommonStatusCodes.API_NOT_CONNECTED) {
                throw apiException
            }
        }
    }

    private suspend fun internalGetENFClientVersion(): Long = suspendCoroutine { cont ->
        client.version
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
}
