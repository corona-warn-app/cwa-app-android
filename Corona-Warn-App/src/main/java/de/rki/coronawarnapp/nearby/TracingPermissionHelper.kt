package de.rki.coronawarnapp.nearby

import android.app.Activity
import android.content.Intent
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TracingPermissionHelper @Inject constructor(
    private val enfClient: ENFClient,
    @AppScope private val scope: CoroutineScope
) {

    var callback: Callback? = null

    fun startTracing(
        onUserPermissionRequired: (permissionRequest: (Activity) -> Unit) -> Unit
    ) {
        scope.launch {
            if (enfClient.isTracingEnabled.first()) {
                callback?.onUpdateTracingStatus(true)
            } else {
                enableTracing(onUserPermissionRequired)
            }
        }
    }

    private fun enableTracing(
        onUserPermissionRequired: ((permissionRequest: (Activity) -> Unit) -> Unit)?
    ) {
        enfClient.setTracing(
            true,
            onSuccess = { callback?.onUpdateTracingStatus(true) },
            onError = { callback?.onError(it) },
            onPermissionRequired = { status ->
                if (onUserPermissionRequired != null) {
                    val permissionRequestTrigger: (Activity) -> Unit = {
                        status.startResolutionForResult(it, TRACING_PERMISSION_REQUESTCODE)
                    }
                    onUserPermissionRequired(permissionRequestTrigger)
                } else {
                    callback?.onError(
                        IllegalStateException("Permission were granted but we are still not allowed to enable tracing.")
                    )
                }
            }
        )
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): UpdateResult {
        Timber.v(
            "handleActivityResult(requesutCode=%d, resultCode=%d, data=%s)",
            requestCode, resultCode, data
        )
        if (requestCode != TRACING_PERMISSION_REQUESTCODE) {
            Timber.tag(TAG).w("Not our request code ($requestCode): %s", data)
            return UpdateResult.UNKNOWN_RESULT
        }

        return if (resultCode == Activity.RESULT_OK) {
            Timber.tag(TAG).w("User granted permission (== RESULT_OK): %s", data)

            enableTracing(null)
            UpdateResult.PERMISSION_AVAILABLE
        } else {
            Timber.tag(TAG).w("User declined permission (!= RESULT_OK): %s", data)

            callback?.onUpdateTracingStatus(false)
            UpdateResult.PERMISSION_DECLINED
        }
    }

    enum class UpdateResult {
        PERMISSION_AVAILABLE,
        PERMISSION_DECLINED,
        UNKNOWN_RESULT
    }

    interface Callback {
        fun onUpdateTracingStatus(isTracingEnabled: Boolean)

        fun onError(error: Throwable)
    }

    companion object {
        private const val TAG = "TracingPermissionHelper"
        const val TRACING_PERMISSION_REQUESTCODE = 3010
    }
}
