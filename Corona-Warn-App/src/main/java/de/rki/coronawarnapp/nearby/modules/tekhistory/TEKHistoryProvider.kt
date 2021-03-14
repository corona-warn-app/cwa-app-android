package de.rki.coronawarnapp.nearby.modules.tekhistory

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion

interface TEKHistoryProvider {

    /**
     * Retrieves key history from the data store on the device for uploading to your
     * internet-accessible server. Calling this method prompts Google Play services to display
     * a dialog, requesting permission from the user to gather and upload their exposure keys.
     * The keys returned include the past 14 days, but not the current day’s key.
     *
     * The permission granted by the user lasts for 24 hours, so the permission dialog appears
     * only once for each 24-hour period, regardless of how many times the method is called
     */
    suspend fun getTEKHistory(): List<TemporaryExposureKey>

    suspend fun getTEKHistoryOrRequestPermission(
        onTEKHistoryAvailable: (List<TemporaryExposureKey>) -> Unit,
        onPermissionRequired: (Status) -> Unit
    )

    /**
     * Pre-Authorize ExposureKeyHistory using
     * [ExposureNotificationClient.requestPreAuthorizedTemporaryExposureKeyHistory].
     *
     * @return true if at least [ENFVersion] is [ENFVersion.V1_8]
     * and [ExposureNotificationClient.requestPreAuthorizedTemporaryExposureKeyHistory] continues successfully,
     * otherwise false.
     *
     * @throws [ApiException]
     */
    suspend fun preAuthorizeExposureKeyHistory(): Boolean
}
