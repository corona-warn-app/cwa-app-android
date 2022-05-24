package de.rki.coronawarnapp.nearby

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class TracingPermissionHelper @AssistedInject constructor(
    @Assisted private val callback: Callback,
    private val enfClient: ENFClient,
    private val tracingSettings: TracingSettings,
    @AppScope private val scope: CoroutineScope
) {

    fun startTracing() {
        scope.launch {
            if (enfClient.isTracingEnabled.first()) {
                callback.onUpdateTracingStatus(true)
            } else {
                if (isConsentGiven()) {
                    enableTracing()
                } else {
                    callback.onTracingConsentRequired { given: Boolean ->
                        Timber.tag(TAG).d("Consent result: $given")
                        if (given) enableTracing()
                    }
                }
            }
        }
    }

    private fun enableTracing() {
        enfClient.setTracing(
            true,
            onSuccess = { callback.onUpdateTracingStatus(true) },
            onError = { callback.onError(it) },
            onPermissionRequired = { status ->
                Timber.tag(TAG).d("Permission is required, starting user resolution.")
                val permissionRequestTrigger: (Activity) -> Unit = {
                    status.startResolutionForResult(it, TRACING_PERMISSION_REQUESTCODE)
                }
                callback.onPermissionRequired(permissionRequestTrigger)
            }
        )
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Timber.v(
            "handleActivityResult(requestCode=%d, resultCode=%d, data=%s)",
            requestCode,
            resultCode,
            data
        )
        if (requestCode != TRACING_PERMISSION_REQUESTCODE) {
            Timber.tag(TAG).w("Not our request code ($requestCode): %s", data)
            return false
        }

        if (resultCode == Activity.RESULT_OK) {
            Timber.tag(TAG).w("User granted permission (== RESULT_OK): %s", data)
            enableTracing()
        } else {
            Timber.tag(TAG).w("User declined permission (!= RESULT_OK): %s", data)
            callback.onUpdateTracingStatus(false)
        }
        return true
    }

    private suspend fun isConsentGiven(): Boolean = tracingSettings.isConsentGiven()

    interface Callback {
        fun onUpdateTracingStatus(isTracingEnabled: Boolean)
        fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit)
        fun onPermissionRequired(permissionRequest: (Activity) -> Unit)
        fun onError(error: Throwable)
    }

    companion object {
        private const val TAG = "TracingPermissionHelper"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val TRACING_PERMISSION_REQUESTCODE = 3010
    }

    @AssistedFactory
    interface Factory {
        fun create(callback: Callback): TracingPermissionHelper
    }
}
