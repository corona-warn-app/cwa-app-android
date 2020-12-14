package de.rki.coronawarnapp.submission.data.tekhistory

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
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
    private val tekHistoryStorage: TEKHistoryStorage,
    private val timeStamper: TimeStamper,
    private val enfClient: ENFClient,
    private val tracingPermissionHelperFactory: TracingPermissionHelper.Factory,
    @AppScope private val scope: CoroutineScope
) {

    private val tracingPermissionHelper by lazy {
        tracingPermissionHelperFactory.create(object : TracingPermissionHelper.Callback {
            override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                if (isTracingEnabled) {
                    updateTEKHistoryOrRequestPermission()
                } else {
                    Timber.tag(TAG).w("Can't start TEK update, tracing permission was declined.")
                }
            }

            override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) =
                callback.onTracingConsentRequired(onConsentResult)

            override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) =
                callback.onPermissionRequired(permissionRequest)

            override fun onError(error: Throwable) = callback.onError(error)
        })
    }

    fun updateTEKHistoryOrRequestPermission() {
        scope.launch {
            if (!enfClient.isTracingEnabled.first()) {
                Timber.tag(TAG).w("Tracing is disabled, enabling...")
                tracingPermissionHelper.startTracing()
            } else {
                updateTEKHistoryInternal()
            }
        }
    }

    private suspend fun updateTEKHistoryInternal() {
        enfClient.getTEKHistoryOrRequestPermission(
            onTEKHistoryAvailable = {
                Timber.tag(TAG).d("TEKS were directly available.")
                updateHistoryAndTriggerCallback(it)
            },
            onPermissionRequired = { status ->
                Timber.tag(TAG).d("TEK request requires user resolution.")
                val permissionRequestTrigger: (Activity) -> Unit = {
                    status.startResolutionForResult(it, TEK_PERMISSION_REQUEST)
                }
                callback.onPermissionRequired(permissionRequestTrigger)
            }
        )
    }

    private fun updateHistoryAndTriggerCallback(availableTEKs: List<TemporaryExposureKey>? = null) {
        scope.launch {
            try {
                val result = updateTEKHistory(availableTEKs)
                callback.onTEKAvailable(result)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    private suspend fun updateTEKHistory(
        availableTEKs: List<TemporaryExposureKey>? = null
    ): List<TemporaryExposureKey> {
        val deferred = scope.async {
            val teks = availableTEKs ?: enfClient.getTEKHistory()
            Timber.i("Permission are available, storing TEK history.")

            teks.also {
                tekHistoryStorage.storeTEKData(
                    TEKHistoryStorage.TEKBatch(
                        batchId = UUID.randomUUID().toString(),
                        obtainedAt = timeStamper.nowUTC,
                        keys = teks
                    )
                )
            }
        }
        return try {
            deferred.await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Positive permission result but failed to update history?")
            e.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
            throw e
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val isTracingPermissionRequest = tracingPermissionHelper.handleActivityResult(requestCode, resultCode, data)
        if (isTracingPermissionRequest) {
            Timber.tag(TAG).d("Was tracing permission request, will try TEK update if tracing is now enabled.")
            return true
        }

        if (requestCode != TEK_PERMISSION_REQUEST) {
            Timber.tag(TAG).w("Not our request code ($requestCode): %s", data)
            return false
        }

        if (resultCode == Activity.RESULT_OK) {
            Timber.tag(TAG).d("We got TEK permission, now updating history.")
            updateHistoryAndTriggerCallback()
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

    @AssistedInject.Factory
    interface Factory {
        fun create(callback: Callback): TEKHistoryUpdater
    }

    companion object {
        private const val TAG = "TEKHistoryUpdater"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val TEK_PERMISSION_REQUEST = 3011
    }
}
