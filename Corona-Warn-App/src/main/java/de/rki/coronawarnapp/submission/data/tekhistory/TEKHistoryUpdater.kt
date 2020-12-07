package de.rki.coronawarnapp.submission.data.tekhistory

import android.app.Activity
import android.content.Intent
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class TEKHistoryUpdater @Inject constructor(
    private val tekHistoryStorage: TEKHistoryStorage,
    private val timeStamper: TimeStamper,
    private val enfClient: ENFClient,
    @AppScope private val scope: CoroutineScope
) {

    var callback: Callback? = null

    fun updateTEKHistoryOrRequestPermission(
        onUserPermissionRequired: (permissionRequest: (Activity) -> Unit) -> Unit
    ) {
        scope.launch {
            enfClient.getTEKHistoryOrRequestPermission(
                onTEKHistoryAvailable = {
                    updateHistoryAndTriggerCallback()
                },
                onPermissionRequired = { status ->
                    val permissionRequestTrigger: (Activity) -> Unit = {
                        status.startResolutionForResult(it, TEK_PERMISSION_REQUESTCODE)
                    }
                    onUserPermissionRequired(permissionRequestTrigger)
                }
            )
        }
    }

    suspend fun updateHistoryOrThrow(): List<TemporaryExposureKey> {
        return updateTEKHistory()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): UpdateResult {
        if (requestCode != TEK_PERMISSION_REQUESTCODE) {
            Timber.tag(TAG).w("Not our request code ($requestCode): %s", data)
            return UpdateResult.UNKNOWN_RESULT
        }
        return if (resultCode == Activity.RESULT_OK) {
            Timber.tag(TAG).w("Permission granted (== RESULT_OK): %s", data)
            updateHistoryAndTriggerCallback()
            UpdateResult.PERMISSION_AVAILABLE
        } else {
            Timber.tag(TAG).w("Permission declined (!= RESULT_OK): %s", data)
            callback?.onPermissionDeclined()
            UpdateResult.PERMISSION_UNAVAILABLE
        }
    }

    private fun updateHistoryAndTriggerCallback() {
        scope.launch {
            try {
                val result = updateTEKHistory()
                callback?.onTEKAvailable(result)
            } catch (e: Exception) {
                callback?.onError(e)
            }
        }
    }

    private suspend fun updateTEKHistory(): List<TemporaryExposureKey> {
        val deferred = scope.async {
            val teks = enfClient.getTEKHistory()
            Timber.i("Permission are available, storing TEK history.")

            tekHistoryStorage.storeTEKData(
                TEKHistoryStorage.TEKBatch(
                    batchId = UUID.randomUUID().toString(),
                    obtainedAt = timeStamper.nowUTC,
                    keys = teks
                )
            )

            teks
        }
        return try {
            deferred.await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Positive permission result but failed to update history?")
            e.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
            throw e
        }
    }

    enum class UpdateResult {
        PERMISSION_AVAILABLE,
        PERMISSION_UNAVAILABLE,
        UNKNOWN_RESULT
    }

    companion object {
        private const val TAG = "TEKHistoryUpdater"
        const val TEK_PERMISSION_REQUESTCODE = 3011
    }

    interface Callback {
        fun onTEKAvailable(teks: List<TemporaryExposureKey>)
        fun onPermissionDeclined()
        fun onError(error: Throwable)
    }
}
