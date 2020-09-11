package de.rki.coronawarnapp.util

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import kotlin.math.abs

class GoogleAPIVersion {
    /**
     * Indicates if the client runs above a certain version
     *
     * @return isAboveVersion, if connected to an old unsupported version, return false
     */
    suspend fun isAbove(compareVersion: Long): Boolean {
        if (!compareVersion.isCorrectVersionLength) {
            throw IllegalArgumentException("given version has incorrect length")
        }
        return try {
            compareVersion > InternalExposureNotificationClient.getVersion()
        } catch (apiException: ApiException) {
            if (apiException.statusCode == CommonStatusCodes.API_NOT_CONNECTED) false
            else throw apiException
        }
    }

    // check if a raw long has the correct length to be considered an API version
    private val Long.isCorrectVersionLength
        get(): Boolean = abs(this).toString().length == GOOGLE_API_VERSION_FIELD_LENGTH

    companion object {
        private const val GOOGLE_API_VERSION_FIELD_LENGTH = 8
        const val V16 = 16000000L
    }
}
