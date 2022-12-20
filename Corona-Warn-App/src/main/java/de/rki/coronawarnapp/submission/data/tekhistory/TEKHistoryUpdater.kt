package de.rki.coronawarnapp.submission.data.tekhistory

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class TEKHistoryUpdater @AssistedInject constructor(
    @Assisted val callback: Callback,
    private val tekCache: TEKHistoryStorage,
    private val timeStamper: TimeStamper,
    private val enfClient: ENFClient,
    private val tracingPermissionHelperFactory: TracingPermissionHelper.Factory,
    @AppScope private val scope: CoroutineScope
) {

    private val tracingPermissionHelper by lazy {
        tracingPermissionHelperFactory.create(
            object : TracingPermissionHelper.Callback {
                override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                    if (isTracingEnabled) {
                        getTeksOrRequestPermission()
                    } else {
                        Timber.tag(TAG).w("Can't start TEK update, tracing permission was declined.")
                        callback.onTEKPermissionDeclined()
                    }
                }

                override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) =
                    callback.onTracingConsentRequired(onConsentResult)

                override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) =
                    callback.onPermissionRequired(permissionRequest)

                override fun onError(error: Throwable) = callback.onError(error)
            }
        )
    }

    suspend fun clearTekCache() {
        tekCache.reset()
    }

    fun getTeksForTesting() {
        scope.launch {
            if (!enfClient.isTracingEnabled.first()) {
                Timber.tag(TAG).w("Tracing is disabled, abort.")
                callback.onError(
                    IllegalStateException("Tracing is disabled. Please enable tracing first via settings.")
                )
                return@launch
            }
            val latestKeys = getCachedKeys()
            // Use cached keys if there are any
            if (latestKeys.isNotEmpty()) {
                callback.onTEKAvailable(latestKeys)
                return@launch
            }
            getTekHistoryOrRequestPermission(false)
        }
    }

    fun getTeksOrRequestPermission() {
        scope.launch {
            if (!enfClient.isTracingEnabled.first()) {
                Timber.tag(TAG).w("Tracing is disabled, enabling...")
                tracingPermissionHelper.startTracing()
            } else {
                val latestKeys = getCachedKeys()
                // Use cached keys if there are any
                if (latestKeys.isNotEmpty()) {
                    callback.onTEKAvailable(latestKeys)
                    return@launch
                }
                getTekHistoryOrRequestPermission(updateCache = true)
            }
        }
    }

    fun getTeksOrRequestPermissionFromOS() {
        scope.launch {
            if (!enfClient.isTracingEnabled.first()) {
                Timber.tag(TAG).w("Tracing is disabled, enabling...")
                tracingPermissionHelper.startTracing()
            } else {
                getTekHistoryOrRequestPermission(updateCache = false)
            }
        }
    }

    private suspend fun getTekHistoryOrRequestPermission(updateCache: Boolean) {
        enfClient.getTEKHistoryOrRequestPermission(
            onTEKHistoryAvailable = {
                Timber.tag(TAG).d("TEKs were directly available.")
                if (updateCache) scope.launch {
                    updateTekCache(it)
                }
                callback.onTEKAvailable(it)
            },
            onPermissionRequired = { status ->
                val requestCode = if (updateCache) TEK_PERMISSION_REQUEST_WITH_CACHING
                else TEK_PERMISSION_REQUEST_NO_CACHING
                Timber.tag(TAG).d("TEK request requires user resolution.")
                val permissionRequestTrigger: (Activity) -> Unit = {
                    status.startResolutionForResult(it, requestCode)
                }
                callback.onPermissionRequired(permissionRequestTrigger)
            }
        )
    }

    private suspend fun getTekHistory(): List<TemporaryExposureKey> {
        val deferred = scope.async {
            enfClient.getTEKHistory()
        }
        return try {
            deferred.await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Positive permission result but failed to update history?")
            e.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
            throw e
        }
    }

    private suspend fun updateTekCache(
        availableTEKs: List<TemporaryExposureKey>
    ) {
        try {
            Timber.tag(TAG).i("Caching TEK history.")
            tekCache.storeTEKData(
                TEKHistoryStorage.TEKBatch(
                    batchId = UUID.randomUUID().toString(),
                    obtainedAt = timeStamper.nowUTC,
                    keys = availableTEKs
                )
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to update history")
            e.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
        }
    }

    private suspend fun getCachedKeys() = tekCache.tekData.first()
        .maxByOrNull { it.obtainedAt }?.keys
        .orEmpty()

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        updateCache: Boolean = true
    ): Boolean {
        val isTracingPermissionRequest = tracingPermissionHelper.handleActivityResult(requestCode, resultCode, data)
        if (isTracingPermissionRequest) {
            Timber.tag(TAG).d("Was tracing permission request, will try TEK update if tracing is now enabled.")
            return true
        }

        if (requestCode !in listOf(TEK_PERMISSION_REQUEST_WITH_CACHING, TEK_PERMISSION_REQUEST_NO_CACHING)) {
            Timber.tag(TAG).w("Not our request code ($requestCode): %s", data)
            return false
        }

        if (resultCode == Activity.RESULT_OK) {
            Timber.tag(TAG).d("We got TEK permission, now updating history.")
            scope.launch {
                val teks = getTekHistory()
                if (requestCode == TEK_PERMISSION_REQUEST_WITH_CACHING && updateCache) updateTekCache(teks)
                callback.onTEKAvailable(teks)
            }
        } else {
            Timber.tag(TAG).i("Permission declined (!= RESULT_OK): %s", data)
            callback.onTEKPermissionDeclined()
        }
        return true
    }

    interface Callback {
        fun onTEKAvailable(teks: List<TemporaryExposureKey>)
        fun onTEKPermissionDeclined()
        fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit)
        fun onPermissionRequired(permissionRequest: (Activity) -> Unit)
        fun onError(error: Throwable)
    }

    @AssistedFactory
    interface Factory {
        fun create(callback: Callback): TEKHistoryUpdater
    }

    companion object {
        private const val TAG = "TEKHistoryUpdater"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val TEK_PERMISSION_REQUEST_WITH_CACHING = 3011

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val TEK_PERMISSION_REQUEST_NO_CACHING = 3033
    }
}
