package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.common.api.Status
import kotlinx.coroutines.flow.Flow

interface TracingStatus {
    val isTracingEnabled: Flow<Boolean>

    fun setTracing(
        enable: Boolean,
        onSuccess: (Boolean) -> Unit,
        onError: (Throwable) -> Unit,
        onPermissionRequired: (Status) -> Unit
    )
}
