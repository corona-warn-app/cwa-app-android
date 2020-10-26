package de.rki.coronawarnapp.nearby.modules.tracing

import kotlinx.coroutines.flow.Flow

interface TracingStatus {
    val isTracingEnabled: Flow<Boolean>
}
