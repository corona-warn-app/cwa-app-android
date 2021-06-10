package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.common.api.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface TracingStatus {
    val isTracingEnabled: Flow<Boolean>

    fun setTracing(
        enable: Boolean,
        onSuccess: (Boolean) -> Unit,
        onError: (Throwable) -> Unit,
        onPermissionRequired: (Status) -> Unit
    )
}

/**
 * Returns true if tracing was disabled.
 */
suspend fun TracingStatus.disableTracingIfEnabled(): Boolean {
    if (!isTracingEnabled.first()) {
        Timber.d("Tracing was already disabled.")
        return false
    }

    return suspendCoroutine { cont ->
        setTracing(
            enable = false,
            onSuccess = { cont.resume(true) },
            onError = { cont.resumeWithException(it) },
            onPermissionRequired = { Timber.e("Permission was required to disable tracing?") }
        )
    }
}
